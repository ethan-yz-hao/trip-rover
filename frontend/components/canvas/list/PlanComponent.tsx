"use client";
import React, { useState, useEffect, useRef } from "react";
import {
    Place,
    PlanPlaces,
    PlanAckMessage,
    PlanUpdateMessage,
    ResponseResult,
} from "@/types/model";
import {
    DragDropContext,
    Droppable,
    Draggable,
    DropResult,
} from "@hello-pangea/dnd";
import WebSocketService from "@/lib/webSocketService";
import { v4 as uuidv4 } from "uuid";
import { axiosInstance, AppError } from "@/lib/axios";
import log from "@/lib/log";
import StaySecondsEditor from "@/components/canvas/list/StaySecondEditor";
import {
    Box,
    CircularProgress,
    Alert,
    Typography,
    Snackbar,
} from "@mui/material";

interface PlanComponentProps {
    planId: number;
}

interface PendingUpdate {
    updateId: string;
    updateMessage: PlanUpdateMessage;
}

const PlanComponent: React.FC<PlanComponentProps> = ({ planId }) => {
    const [planPlaces, setPlanPlaces] = useState<PlanPlaces | null>(null);
    const planPlacesRef = useRef<PlanPlaces | null>(null);
    const webSocketServiceRef = useRef<WebSocketService | null>(null);
    const [newGooglePlaceId, setNewGooglePlaceId] = useState<string>("");
    const [error, setError] = useState<string | null>(null);
    const [loading, setLoading] = useState(true);

    // Ordered queue of pending updates
    const pendingUpdatesRef = useRef<PendingUpdate[]>([]);

    // fetch the plan places data
    const fetchPlanData = async () => {
        try {
            setLoading(true);

            const response = await axiosInstance.get<
                ResponseResult<PlanPlaces>
            >(`/plan/${planId}/places`);

            setPlanPlaces(response.data.data);
            planPlacesRef.current = response.data.data;
        } catch (err) {
            log.error("Error fetching plan:", err);
            if (err instanceof AppError) {
                setError(err.message);
            } else {
                setError("Failed to load the plan. Please try again.");
            }
        } finally {
            setLoading(false);
        }
    };

    // Load the plan places data
    useEffect(() => {
        fetchPlanData();
    }, [planId]);

    // Initialize and manage WebSocket connection
    useEffect(() => {
        if (webSocketServiceRef.current) return; // avoid re-initializing

        const webSocketService = new WebSocketService(planId);
        webSocketServiceRef.current = webSocketService;

        const initializeWebSocket = async () => {
            try {
                await webSocketService.connect(
                    (updateMessage: PlanUpdateMessage) => {
                        log.info(
                            "WebSocket update message received:",
                            updateMessage
                        );
                        handleUpdateMessage(updateMessage);
                    },
                    (ackMessage: PlanAckMessage) => {
                        log.info("WebSocket ack message received:", ackMessage);
                        handleAckMessage(ackMessage);
                    }
                );
            } catch (err) {
                log.error("WebSocket connection error:", err);
                if (err instanceof AppError) {
                    setError(err.message);
                } else {
                    setError(
                        "Failed to establish real-time connection. Updates may be delayed."
                    );
                }
            }
        };

        initializeWebSocket();

        return () => {
            webSocketService.disconnect();
        };
    }, [planId]);

    // Handle incoming plan updates from other clients
    const handleUpdateMessage = (updateMessage: PlanUpdateMessage) => {
        const { clientId, updateId, version } = updateMessage;

        // If the update originated from this client, remove it from pending updates and do not re-apply, but update the plan version
        if (clientId === webSocketServiceRef.current?.getClientId()) {
            const pendingIndex = pendingUpdatesRef.current.findIndex(
                (update) => update.updateId === updateId
            );
            if (pendingIndex !== -1) {
                pendingUpdatesRef.current.splice(pendingIndex, 1);
            }

            // Update the plan version
            setPlanPlaces((prevPlanPlaces) => {
                if (!prevPlanPlaces) return prevPlanPlaces;
                return { ...prevPlanPlaces, version };
            });

            return;
        }

        // Apply the update from other clients
        if (!planPlacesRef.current) {
            setError("Cannot apply update: Plan not loaded");
            return;
        }

        try {
            const updatedPlan = applyLocalUpdate(
                planPlacesRef.current,
                updateMessage
            );
            if (!updatedPlan) {
                throw new AppError("Failed to apply incoming update", 422);
            }

            planPlacesRef.current = updatedPlan;
            setPlanPlaces(updatedPlan);
        } catch (err) {
            log.error("Failed to apply incoming update:", err);
            if (err instanceof AppError) {
                setError(err.message);
            } else {
                setError("Failed to apply incoming update");
            }
            fetchPlanData(); // Reload the plan as fallback
        }
    };

    // Handle incoming ack messages
    const handleAckMessage = (ackMessage: PlanAckMessage) => {
        const { updateId, status, errorMessage } = ackMessage;

        // Remove the update from the pending updates
        const pendingIndex = pendingUpdatesRef.current.findIndex(
            (update) => update.updateId === updateId
        );
        if (pendingIndex !== -1) {
            pendingUpdatesRef.current.splice(pendingIndex, 1);
        }

        if (status === "ERROR") {
            log.error("Update failed:", errorMessage);
            setError(errorMessage || "Update failed");
            fetchPlanData(); // Reload the plan
        }
    };

    // Apply update to the plan locally (used by both optimistic updates and incoming updates from other clients)
    const applyLocalUpdate = (
        currentPlan: PlanPlaces,
        updateMessage: PlanUpdateMessage
    ): PlanPlaces | null => {
        try {
            switch (updateMessage.action) {
                case "REORDER":
                    const { placeId, targetPlaceId, version } = updateMessage;
                    const placeToMoveIndex = currentPlan.places.findIndex(
                        (place) => place.placeId === placeId
                    );
                    if (placeToMoveIndex === -1) {
                        return null;
                    }

                    const placeToMove = currentPlan.places[placeToMoveIndex];
                    const updatedPlaces = Array.from(currentPlan.places);
                    updatedPlaces.splice(placeToMoveIndex, 1); // Remove the place

                    let targetIndex = updatedPlaces.length; // Default to end
                    if (targetPlaceId) {
                        const targetPlace = updatedPlaces.find(
                            (place) => place.placeId === targetPlaceId
                        );
                        if (targetPlace) {
                            targetIndex = updatedPlaces.indexOf(targetPlace); // Insert before the targetIndex
                        }
                    }

                    // Adjust target index (insert after the targetIndex) if moving down
                    if (targetIndex >= placeToMoveIndex) {
                        targetIndex += 1;
                    }

                    updatedPlaces.splice(targetIndex, 0, placeToMove); // Insert at new position

                    return { ...currentPlan, places: updatedPlaces, version };

                case "ADD":
                    if (updateMessage.placeId && updateMessage.googlePlaceId) {
                        const newPlace: Place = {
                            placeId: updateMessage.placeId,
                            googlePlaceId: updateMessage.googlePlaceId,
                            staySeconds: updateMessage.staySeconds || 1800,
                        };
                        return {
                            ...currentPlan,
                            places: [...currentPlan.places, newPlace],
                            version: updateMessage.version,
                        };
                    }
                    return null;

                case "REMOVE":
                    if (updateMessage.placeId) {
                        const filteredPlaces = currentPlan.places.filter(
                            (place) => place.placeId !== updateMessage.placeId
                        );
                        return {
                            ...currentPlan,
                            places: filteredPlaces,
                            version: updateMessage.version,
                        };
                    }
                    return null;

                case "UPDATE":
                    if (
                        updateMessage.placeId &&
                        updateMessage.googlePlaceId &&
                        updateMessage.staySeconds
                    ) {
                        const updatedPlaces = currentPlan.places.map(
                            (place) => {
                                if (place.placeId === updateMessage.placeId) {
                                    return {
                                        ...place,
                                        googlePlaceId:
                                            updateMessage.googlePlaceId!,
                                        staySeconds: updateMessage.staySeconds!,
                                    };
                                }
                                return place;
                            }
                        );
                        return {
                            ...currentPlan,
                            places: updatedPlaces,
                            version: updateMessage.version,
                        };
                    }
                    return null;
                default:
                    throw new AppError(
                        `Unknown action: ${updateMessage.action}`,
                        422
                    );
            }
        } catch (err) {
            if (err instanceof AppError) {
                throw err;
            }
            throw new AppError("Failed to apply update", 422, err);
        }
    };

    // Send an update message
    const sendUpdate = (updateMessage: PlanUpdateMessage) => {
        try {
            if (!webSocketServiceRef.current?.isConnected()) {
                throw new AppError(
                    "WebSocket connection is not available",
                    409
                );
            }

            const updateId = uuidv4();

            if (!planPlacesRef.current) {
                throw new AppError("Plan data is not loaded", 409);
            }

            // Optimistically apply the update
            const updatedPlan = applyLocalUpdate(
                planPlacesRef.current,
                updateMessage
            );
            if (!updatedPlan) {
                throw new AppError("Failed to apply local update", 422);
            }

            planPlacesRef.current = updatedPlan;
            setPlanPlaces(updatedPlan);

            // Send the update via WebSocket
            pendingUpdatesRef.current.push({ updateId, updateMessage });
            webSocketServiceRef.current.sendUpdate(updateMessage, updateId);
        } catch (err) {
            log.error("Error sending update:", err);
            if (err instanceof AppError) {
                setError(err.message);
            } else {
                setError("Failed to send update. Please try again.");
            }
            // Revert optimistic update if needed
            if (planPlacesRef.current) {
                setPlanPlaces(planPlacesRef.current);
            }
        }
    };

    // Handle drag and drop
    const onDragEnd = (result: DropResult) => {
        if (!result.destination || !planPlaces) {
            return;
        }

        const sourceIndex = result.source.index;
        const destinationIndex = result.destination.index;

        if (sourceIndex === destinationIndex) {
            return;
        }

        const movedPlace = planPlaces.places[sourceIndex];
        const targetPlace = planPlaces.places[destinationIndex];

        // Send update to the server via WebSocket
        const updateMessage: PlanUpdateMessage = {
            action: "REORDER",
            placeId: movedPlace.placeId,
            targetPlaceId: targetPlace.placeId,
            clientId: webSocketServiceRef.current?.getClientId() || "",
            updateId: uuidv4(),
            version: planPlaces.version,
        };

        sendUpdate(updateMessage);
    };

    // Handle error snackbar close
    const handleErrorClose = (
        event?: React.SyntheticEvent | Event,
        reason?: string
    ) => {
        if (reason === "clickaway") {
            return;
        }
        setError(null);
    };

    if (loading) {
        return (
            <Box display="flex" justifyContent="center" p={4}>
                <CircularProgress />
            </Box>
        );
    }

    if (!planPlaces || !planPlaces.places) {
        return (
            <Box p={2}>
                <Typography>No plan data available.</Typography>
            </Box>
        );
    }

    const handleAdd = async (e: React.FormEvent) => {
        e.preventDefault();

        // Send update to the server via WebSocket
        const updateMessage: PlanUpdateMessage = {
            clientId: webSocketServiceRef.current?.getClientId() || "",
            updateId: uuidv4(),
            action: "ADD",
            placeId: uuidv4(),
            version: planPlaces.version,
            googlePlaceId: newGooglePlaceId,
            staySeconds: 1800,
        };

        sendUpdate(updateMessage);

        setNewGooglePlaceId("");
    };

    const handleDelete = async (placeId: string) => {
        const index = planPlaces.places.findIndex(
            (place) => place.placeId === placeId
        );
        if (index === -1) {
            return;
        }

        // Send update to the server via WebSocket
        const updateMessage: PlanUpdateMessage = {
            clientId: webSocketServiceRef.current?.getClientId() || "",
            updateId: uuidv4(),
            action: "REMOVE",
            placeId,
            version: planPlaces.version,
        };

        sendUpdate(updateMessage);
    };

    const handleStaySecondsUpdate = async (
        placeId: string,
        newStaySeconds: number
    ) => {
        const place = planPlaces.places.find(
            (place) => place.placeId === placeId
        );
        if (!place) {
            return;
        }

        // Send update to the server via WebSocket
        const updateMessage: PlanUpdateMessage = {
            clientId: webSocketServiceRef.current?.getClientId() || "",
            updateId: uuidv4(),
            action: "UPDATE",
            placeId,
            version: planPlaces.version,
            googlePlaceId: place.googlePlaceId,
            staySeconds: newStaySeconds,
        };

        sendUpdate(updateMessage);
    };

    return (
        <Box>
            <Snackbar
                open={error !== null}
                autoHideDuration={6000}
                onClose={handleErrorClose}
                anchorOrigin={{ vertical: "bottom", horizontal: "center" }}
            >
                <Alert
                    onClose={handleErrorClose}
                    severity="error"
                    variant="filled"
                    sx={{ width: "100%" }}
                >
                    {error}
                </Alert>
            </Snackbar>
            <form onSubmit={handleAdd}>
                <div>
                    <label htmlFor="newGooglePlaceId">
                        New Google Place Id:
                    </label>
                    <input
                        type="text"
                        id="newGooglePlaceId"
                        value={newGooglePlaceId}
                        onChange={(e) => setNewGooglePlaceId(e.target.value)}
                        required
                    />
                </div>
                <button type="submit">Add New Place Index</button>
            </form>
            <DragDropContext onDragEnd={onDragEnd}>
                <Droppable droppableId="places">
                    {(provided) => (
                        <ul
                            {...provided.droppableProps}
                            ref={provided.innerRef}
                        >
                            {planPlaces.places.map((place, index) => (
                                <Draggable
                                    key={place.placeId}
                                    draggableId={place.placeId}
                                    index={index}
                                >
                                    {(provided) => (
                                        <li
                                            ref={provided.innerRef}
                                            {...provided.draggableProps}
                                            {...provided.dragHandleProps}
                                        >
                                            {place.placeId}{" "}
                                            {place.googlePlaceId}{" "}
                                            {place.staySeconds}{" "}
                                            <StaySecondsEditor
                                                placeId={place.placeId}
                                                currentStaySeconds={
                                                    place.staySeconds
                                                }
                                                onSubmit={
                                                    handleStaySecondsUpdate
                                                }
                                            />
                                            <button
                                                onClick={() =>
                                                    handleDelete(place.placeId)
                                                }
                                            >
                                                Delete
                                            </button>
                                        </li>
                                    )}
                                </Draggable>
                            ))}
                            {provided.placeholder}
                        </ul>
                    )}
                </Droppable>
            </DragDropContext>
        </Box>
    );
};

export default PlanComponent;
