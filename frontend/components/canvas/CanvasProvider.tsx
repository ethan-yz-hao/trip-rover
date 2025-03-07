import React, {
    createContext,
    useContext,
    useEffect,
    useState,
    useRef,
} from "react";
import { useAppSelector } from "@/lib/hooks";
import WebSocketService from "@/lib/webSocketService";
import {
    PlanUpdateMessage,
    PlanAckMessage,
    PlanPlaces,
    PlanSummary,
    ResponseResult,
} from "@/types/model";
import { v4 as uuidv4 } from "uuid";
import log from "@/lib/log";
import { axiosInstance, AppError } from "@/lib/axios";

interface PendingUpdate {
    updateId: string;
    updateMessage: PlanUpdateMessage;
}

interface CanvasContextType {
    isAuthenticated: boolean;
    webSocketService: WebSocketService | null;
    planId: number | null;
    isConnected: boolean;
    planPlaces: PlanPlaces | null;
    planSummary: PlanSummary | null;
    loading: boolean;
    error: string | null;
    userRole: "OWNER" | "EDITOR" | "VIEWER";
    setUserRole: (role: "OWNER" | "EDITOR" | "VIEWER") => void;
    // Methods for both authenticated and unauthenticated modes
    sendUpdate: (updateMessage: PlanUpdateMessage) => void;
    setPlanSummary: (summary: PlanSummary) => void;
    clearError: () => void;
}

const CanvasContext = createContext<CanvasContextType>({
    isAuthenticated: false,
    webSocketService: null,
    planId: null,
    isConnected: false,
    planPlaces: null,
    planSummary: null,
    loading: false,
    error: null,
    userRole: "VIEWER",
    setUserRole: () => {},
    sendUpdate: () => {},
    setPlanSummary: () => {},
    clearError: () => {},
});

export const useCanvasContext = () => useContext(CanvasContext);

export const CanvasProvider: React.FC<{
    children: React.ReactNode;
    planId?: number;
}> = ({ children, planId = undefined }) => {
    const { isAuthenticated } = useAppSelector((state) => state.auth);
    const [webSocketService, setWebSocketService] =
        useState<WebSocketService | null>(null);
    const [isConnected, setIsConnected] = useState(false);
    const [planPlaces, setPlanPlaces] = useState<PlanPlaces | null>(null);
    const [planSummary, setPlanSummary] = useState<PlanSummary | null>(null);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const [userRole, setUserRole] = useState<"OWNER" | "EDITOR" | "VIEWER">(
        "VIEWER"
    );

    // Refs for tracking state in callbacks
    const planPlacesRef = useRef<PlanPlaces | null>(null);
    const webSocketServiceRef = useRef<WebSocketService | null>(null);
    const pendingUpdatesRef = useRef<PendingUpdate[]>([]);

    // Update refs when state changes
    useEffect(() => {
        planPlacesRef.current = planPlaces;
    }, [planPlaces]);

    useEffect(() => {
        webSocketServiceRef.current = webSocketService;
    }, [webSocketService]);

    // Fetch plan data from API
    const fetchPlanData = async () => {
        console.log("fetchPlanData");
        if (!planId) return;

        try {
            setLoading(true);
            setError(null);

            const response = await axiosInstance.get<{
                data: PlanPlaces;
            }>(`/plan/${planId}/places`);

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

    // Fetch plan summary from API
    const fetchPlanSummary = async () => {
        if (!planId) return;

        try {
            setLoading(true);
            setError(null);

            const response = await axiosInstance.get<
                ResponseResult<PlanSummary>
            >(`/plan/${planId}`);

            // Convert string dates to Date objects
            const summary = {
                ...response.data.data,
                createTime: new Date(response.data.data.createTime),
                updateTime: new Date(response.data.data.updateTime),
            };

            setPlanSummary(summary);
            setUserRole(summary.role);
        } catch (err) {
            log.error("Error fetching plan summary:", err);
            if (err instanceof AppError) {
                setError(err.message);
            } else {
                setError("Failed to load plan summary");
            }
        } finally {
            setLoading(false);
        }
    };

    // Initialize WebSocket connection for authenticated users
    useEffect(() => {
        // Only create WebSocket service if authenticated and we have a planId
        if (isAuthenticated && planId) {
            const service = new WebSocketService(planId);
            setWebSocketService(service);
            webSocketServiceRef.current = service;

            // Connect to WebSocket
            service
                .connect(
                    // Handle update messages
                    (updateMessage: PlanUpdateMessage) => {
                        log.info(
                            "WebSocket update message received:",
                            updateMessage
                        );
                        handleUpdateMessage(updateMessage);
                    },
                    // Handle ack messages
                    (ackMessage: PlanAckMessage) => {
                        log.info("WebSocket ack message received:", ackMessage);
                        handleAckMessage(ackMessage);
                    }
                )
                .then(() => {
                    setIsConnected(true);
                })
                .catch((error) => {
                    log.error("WebSocket connection failed:", error);
                    setError(
                        "Failed to establish real-time connection. Updates may be delayed."
                    );
                });

            return () => {
                service.disconnect();
                setWebSocketService(null);
                webSocketServiceRef.current = null;
                setIsConnected(false);
            };
        }
    }, [isAuthenticated, planId]);

    // Load initial plan data and summary
    useEffect(() => {
        if (planId) {
            fetchPlanData();
            fetchPlanSummary();
        }
    }, [planId]);

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

                    updatedPlaces.splice(targetIndex, 0, placeToMove); // Insert at new position

                    return { ...currentPlan, places: updatedPlaces, version };

                case "ADD":
                    if (updateMessage.placeId && updateMessage.googlePlaceId) {
                        const newPlace = {
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

    // Send an update message - works in both authenticated and unauthenticated modes
    const sendUpdate = (updateMessage: PlanUpdateMessage) => {
        try {
            if (!planPlacesRef.current) {
                throw new AppError("Plan data is not loaded", 409);
            }

            const updateId = uuidv4();

            // For authenticated users with WebSocket
            if (isAuthenticated && webSocketServiceRef.current?.isConnected()) {
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
            }
            // For unauthenticated users or when WebSocket is not available
            else {
                // Apply update locally only
                const updatedPlan = applyLocalUpdate(
                    planPlacesRef.current,
                    updateMessage
                );
                if (!updatedPlan) {
                    throw new AppError("Failed to apply local update", 422);
                }

                planPlacesRef.current = updatedPlan;
                setPlanPlaces(updatedPlan);

                // Store in localStorage for unauthenticated users
                if (!isAuthenticated && planId) {
                    try {
                        const localStorageKey = `plan_${planId}`;
                        localStorage.setItem(
                            localStorageKey,
                            JSON.stringify(updatedPlan)
                        );
                    } catch (err) {
                        log.error("Failed to save to localStorage:", err);
                    }
                }
            }
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

    const clearError = () => setError(null);

    return (
        <CanvasContext.Provider
            value={{
                isAuthenticated,
                webSocketService,
                planId: planId || null,
                isConnected,
                planPlaces,
                planSummary,
                loading,
                error,
                userRole,
                setUserRole,
                sendUpdate,
                setPlanSummary,
                clearError,
            }}
        >
            {children}
        </CanvasContext.Provider>
    );
};
