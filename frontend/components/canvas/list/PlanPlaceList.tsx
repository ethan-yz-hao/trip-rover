"use client";
import React, { useState, useEffect } from "react";
import { PlanUpdateMessage, PlanPlaces } from "@/types/model";
import {
    DragDropContext,
    Droppable,
    Draggable,
    DropResult,
} from "@hello-pangea/dnd";
import { v4 as uuidv4 } from "uuid";
import StaySecondsEditor from "@/components/canvas/list/StaySecondEditor";
import {
    Box,
    CircularProgress,
    Alert,
    Typography,
    Snackbar,
    TextField,
    Button,
    List,
    ListItem,
    ListItemText,
    IconButton,
} from "@mui/material";
import DeleteIcon from "@mui/icons-material/Delete";
import DragIndicatorIcon from "@mui/icons-material/DragIndicator";
import { useCanvasContext } from "@/components/canvas/CanvasProvider";

const PlanPlaceList: React.FC = () => {
    const {
        isAuthenticated,
        planPlaces,
        loading,
        error,
        userRole,
        sendUpdate,
        clearError,
    } = useCanvasContext();

    const [newGooglePlaceId, setNewGooglePlaceId] = useState<string>("");
    const [localPlanPlaces, setLocalPlanPlaces] = useState<PlanPlaces | null>(
        null
    );

    // Initialize empty plan for unauthenticated users or authenticated users with no plan
    useEffect(() => {
        if (!planPlaces && !loading) {
            // Check localStorage first
            const localStorageKey = "local_plan";
            const savedPlan = localStorage.getItem(localStorageKey);

            if (savedPlan) {
                try {
                    setLocalPlanPlaces(JSON.parse(savedPlan));
                } catch (err) {
                    console.error("Failed to parse saved plan:", err);
                    initializeEmptyPlan();
                }
            } else {
                initializeEmptyPlan();
            }
        } else if (planPlaces) {
            setLocalPlanPlaces(null); // Use the server data when available
        }
    }, [planPlaces, loading]);

    // Initialize an empty plan
    const initializeEmptyPlan = () => {
        const emptyPlan: PlanPlaces = {
            planId: -1,
            planName: "",
            places: [],
            version: 1,
        };
        setLocalPlanPlaces(emptyPlan);

        // Save to localStorage
        const localStorageKey = "local_plan";
        try {
            localStorage.setItem(localStorageKey, JSON.stringify(emptyPlan));
        } catch (err) {
            console.error("Failed to save to localStorage:", err);
        }
    };

    // Determine if user can edit based on role or authentication status
    // For unauthenticated users, always allow editing (local only)
    const canEdit =
        !isAuthenticated || userRole === "OWNER" || userRole === "EDITOR";

    // Get the effective plan data (server data or local data)
    const effectivePlanPlaces = planPlaces || localPlanPlaces;

    // Handle drag and drop
    const onDragEnd = (result: DropResult) => {
        if (
            !result.destination ||
            !effectivePlanPlaces ||
            effectivePlanPlaces.places.length < 2
        ) {
            return;
        }

        const sourceIndex = result.source.index;
        const destinationIndex = result.destination.index;

        if (sourceIndex === destinationIndex) {
            return;
        }

        const movedPlace = effectivePlanPlaces.places[sourceIndex];
        const targetPlace = effectivePlanPlaces.places[destinationIndex];

        // Create update message
        const updateMessage: PlanUpdateMessage = {
            action: "REORDER",
            placeId: movedPlace.placeId,
            targetPlaceId: targetPlace.placeId,
            clientId: uuidv4(), // This will be overridden in the provider for authenticated users
            updateId: uuidv4(),
            version: effectivePlanPlaces.version,
        };

        sendUpdate(updateMessage);
    };

    // Handle error snackbar close
    const handleErrorClose = () => {
        clearError();
    };

    const handleAdd = (e: React.FormEvent) => {
        e.preventDefault();

        if (!effectivePlanPlaces) return;

        // Create update message for adding a place
        const updateMessage: PlanUpdateMessage = {
            clientId: uuidv4(),
            updateId: uuidv4(),
            action: "ADD",
            placeId: uuidv4(),
            version: effectivePlanPlaces.version,
            googlePlaceId: newGooglePlaceId,
            staySeconds: 1800,
        };

        sendUpdate(updateMessage);
        setNewGooglePlaceId("");
    };

    const handleDelete = (placeId: string) => {
        if (!effectivePlanPlaces) return;

        // Create update message for removing a place
        const updateMessage: PlanUpdateMessage = {
            clientId: uuidv4(),
            updateId: uuidv4(),
            action: "REMOVE",
            placeId,
            version: effectivePlanPlaces.version,
        };

        sendUpdate(updateMessage);
    };

    const handleStaySecondsUpdate = (
        placeId: string,
        newStaySeconds: number
    ) => {
        if (!effectivePlanPlaces) return;

        const place = effectivePlanPlaces.places.find(
            (place) => place.placeId === placeId
        );
        if (!place) return;

        // Create update message for updating a place
        const updateMessage: PlanUpdateMessage = {
            clientId: uuidv4(),
            updateId: uuidv4(),
            action: "UPDATE",
            placeId,
            version: effectivePlanPlaces.version,
            googlePlaceId: place.googlePlaceId,
            staySeconds: newStaySeconds,
        };

        sendUpdate(updateMessage);
    };

    if (loading) {
        return (
            <Box display="flex" justifyContent="center" p={4}>
                <CircularProgress />
            </Box>
        );
    }

    if (!effectivePlanPlaces) {
        return (
            <Box p={2}>
                <Typography>Loading plan data...</Typography>
            </Box>
        );
    }

    return (
        <>
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

            {canEdit && (
                <Box component="form" onSubmit={handleAdd}>
                    <TextField
                        label="New Google Place ID"
                        value={newGooglePlaceId}
                        onChange={(e) => setNewGooglePlaceId(e.target.value)}
                        required
                        fullWidth
                        size="small"
                    />
                    <Button
                        type="submit"
                        variant="contained"
                        fullWidth
                        disabled={!newGooglePlaceId}
                    >
                        Add New Place
                    </Button>
                </Box>
            )}

            <DragDropContext onDragEnd={canEdit ? onDragEnd : () => {}}>
                <Droppable droppableId="places">
                    {(provided) => (
                        <List
                            {...provided.droppableProps}
                            ref={provided.innerRef}
                            sx={{
                                bgcolor: "background.paper",
                                borderRadius: 1,
                                boxShadow: 1,
                            }}
                        >
                            {effectivePlanPlaces.places.length === 0 ? (
                                <ListItem>
                                    <ListItemText primary="No places added yet. Add your first place above." />
                                </ListItem>
                            ) : (
                                effectivePlanPlaces.places.map(
                                    (place, index) => (
                                        <Draggable
                                            key={place.placeId}
                                            draggableId={place.placeId}
                                            index={index}
                                            isDragDisabled={!canEdit}
                                        >
                                            {(provided) => (
                                                <ListItem
                                                    ref={provided.innerRef}
                                                    {...provided.draggableProps}
                                                    divider
                                                    secondaryAction={
                                                        canEdit && (
                                                            <IconButton
                                                                edge="end"
                                                                onClick={() =>
                                                                    handleDelete(
                                                                        place.placeId
                                                                    )
                                                                }
                                                                color="error"
                                                            >
                                                                <DeleteIcon />
                                                            </IconButton>
                                                        )
                                                    }
                                                >
                                                    <Box
                                                        {...provided.dragHandleProps}
                                                        sx={{ mr: 1 }}
                                                    >
                                                        <DragIndicatorIcon color="action" />
                                                    </Box>
                                                    <ListItemText
                                                        primary={
                                                            place.googlePlaceId
                                                        }
                                                        secondary={
                                                            <Box sx={{ mt: 1 }}>
                                                                {canEdit ? (
                                                                    <StaySecondsEditor
                                                                        placeId={
                                                                            place.placeId
                                                                        }
                                                                        currentStaySeconds={
                                                                            place.staySeconds
                                                                        }
                                                                        onSubmit={
                                                                            handleStaySecondsUpdate
                                                                        }
                                                                    />
                                                                ) : (
                                                                    <Typography variant="body2">
                                                                        Stay
                                                                        time:{" "}
                                                                        {
                                                                            place.staySeconds
                                                                        }{" "}
                                                                        seconds
                                                                    </Typography>
                                                                )}
                                                            </Box>
                                                        }
                                                    />
                                                </ListItem>
                                            )}
                                        </Draggable>
                                    )
                                )
                            )}
                            {provided.placeholder}
                        </List>
                    )}
                </Droppable>
            </DragDropContext>

            {!isAuthenticated && (
                <Alert severity="info" sx={{ mt: 2 }}>
                    You are working in offline mode. Sign in to save your
                    changes permanently.
                </Alert>
            )}
        </>
    );
};

export default PlanPlaceList;
