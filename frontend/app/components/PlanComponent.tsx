'use client';
import React, {useState, useEffect, useRef} from 'react';
import {Place, Plan, PlanAckMessage, PlanUpdateMessage} from '@/app/model';
import {DragDropContext, Droppable, Draggable, DropResult} from '@hello-pangea/dnd';
import WebSocketService from '@/app/webSocketService';
import {v4 as uuidv4} from 'uuid';
import axios from "axios";
import log from "@/app/log";
import StaySecondsEditor from "@/app/components/StaySecondEditor";

interface PlanComponentProps {
    planId: number;
}

interface PendingUpdate {
    updateId: string;
    updateMessage: PlanUpdateMessage;
}

const PlanComponent: React.FC<PlanComponentProps> = ({planId}) => {
    const backendUrl = process.env.NEXT_PUBLIC_BACKEND_URL;
    const [plan, setPlan] = useState<Plan | null>(null);
    const planRef = useRef<Plan | null>(null);
    const webSocketServiceRef = useRef<WebSocketService | null>(null);
    const [newGooglePlaceId, setNewGooglePlaceId] = useState<string>('');

    // Ordered queue of pending updates
    const pendingUpdatesRef = useRef<PendingUpdate[]>([]);

    // fetch the plan data
    const fetchPlanData = async () => {
        try {
            const response = await axios.get<Plan>(`${backendUrl}/api/plan/${planId}/places`, {
                withCredentials: true,
            });
            setPlan(response.data);
            planRef.current = response.data;
        } catch (error) {
            log.error('Error fetching plan:', error);
            alert('Failed to load the plan. Please try again.');
        }
    };

    // Load the plan data
    useEffect(() => {
        fetchPlanData();
    }, [planId, backendUrl]);

    // Initialize and manage WebSocket connection
    useEffect(() => {
        if (webSocketServiceRef.current) return;  // avoid re-initializing

        const webSocketService = new WebSocketService(planId);
        webSocketServiceRef.current = webSocketService;

        webSocketService.connect(
            (updateMessage: PlanUpdateMessage) => {
            log.info('WebSocket update message received:', updateMessage);
            handleUpdateMessage(updateMessage);
        },
        (ackMessage: PlanAckMessage) => {
            log.log('WebSocket ack message received:', ackMessage);
            handleAckMessage(ackMessage);
        }
        );

        return () => {
            webSocketService.disconnect();
        };
    }, [planId]);

    // Handle incoming plan updates from other clients
    const handleUpdateMessage = (updateMessage: PlanUpdateMessage) => {
        const {clientId, updateId, version} = updateMessage;

        // If the update originated from this client, remove it from pending updates and do not re-apply, but update the plan version
        if (clientId === webSocketServiceRef.current?.getClientId()) {
            const pendingIndex = pendingUpdatesRef.current.findIndex(update => update.updateId === updateId);
            if (pendingIndex !== -1) {
                pendingUpdatesRef.current.splice(pendingIndex, 1);
            }

            // Update the plan version
            setPlan(prevPlan => {
                if (!prevPlan) return prevPlan;
                return {...prevPlan, version};
            });

            return;
        }

        // Apply the update from other clients
        if (!planRef.current) {
            log.error('Plan not loaded');
            return;
        }
        const updatedPlan = applyLocalUpdate(planRef.current, updateMessage);
        if (!updatedPlan) {
            log.error('Failed to apply incoming update locally. Refetching plan data');
            fetchPlanData(); // Reload the plan
        } else {
            planRef.current = updatedPlan;
            setPlan(updatedPlan);
        }
    };

    // Handle incoming ack messages
    const handleAckMessage = (ackMessage: PlanAckMessage) => {
        const {updateId, status, errorMessage} = ackMessage;

        // Remove the update from the pending updates
        const pendingIndex = pendingUpdatesRef.current.findIndex(update => update.updateId === updateId);
        if (pendingIndex !== -1) {
            pendingUpdatesRef.current.splice(pendingIndex, 1);
        }

        if (status === 'ERROR') {
            log.error('Update failed:', errorMessage);
            alert('Update failed: ' + errorMessage);
            fetchPlanData(); // Reload the plan
        }
    }

    // Apply update to the plan locally (used by both optimistic updates and incoming updates from other clients)
    const applyLocalUpdate = (currentPlan: Plan, updateMessage: PlanUpdateMessage): Plan | null => {
        switch (updateMessage.action) {
            case 'REORDER':
                const { placeId, targetPlaceId } = updateMessage;
                const placeToMoveIndex = currentPlan.places.findIndex(place => place.placeId === placeId);
                if (placeToMoveIndex === -1) {
                    log.error(`Place with ID ${placeId} not found for REORDER`);
                    return null;
                }

                const placeToMove = currentPlan.places[placeToMoveIndex];
                const updatedPlaces = Array.from(currentPlan.places);
                updatedPlaces.splice(placeToMoveIndex, 1); // Remove the place

                let targetIndex = updatedPlaces.length; // Default to end
                if (targetPlaceId) {
                    const targetPlace = updatedPlaces.find(place => place.placeId === targetPlaceId);
                    if (targetPlace) {
                        targetIndex = updatedPlaces.indexOf(targetPlace); // Insert before the targetIndex
                    }
                }

                // Adjust target index (insert after the targetIndex) if moving down
                if (targetIndex >= placeToMoveIndex) {
                    targetIndex += 1;
                }

                updatedPlaces.splice(targetIndex, 0, placeToMove); // Insert at new position

                return { ...currentPlan, places: updatedPlaces };

            case 'ADD':
                if (updateMessage.placeId && updateMessage.googlePlaceId) {
                    const newPlace: Place = {
                        placeId: updateMessage.placeId,
                        googlePlaceId: updateMessage.googlePlaceId,
                        staySeconds: updateMessage.staySeconds || 1800,

                    };
                    return { ...currentPlan, places: [...currentPlan.places, newPlace] };
                }
                return null;

            case 'REMOVE':
                if (updateMessage.placeId) {
                    const filteredPlaces = currentPlan.places.filter(place => place.placeId !== updateMessage.placeId);
                    return { ...currentPlan, places: filteredPlaces };
                }
                return null;

            case 'UPDATE':
                if (updateMessage.placeId && updateMessage.googlePlaceId && updateMessage.staySeconds) {
                    const updatedPlaces = currentPlan.places.map(place => {
                        if (place.placeId === updateMessage.placeId) {
                            return {
                                ...place,
                                googlePlaceId: updateMessage.googlePlaceId!,
                                staySeconds: updateMessage.staySeconds!,
                            };
                        }
                        return place;
                    });
                    return { ...currentPlan, places: updatedPlaces };
                }
                return null;
            default:
                log.error('Unknown action:', updateMessage.action);
                return null;
        }
    };

    // Send an update message
    const sendUpdate = (updateMessage: PlanUpdateMessage) => {
        const updateId = uuidv4();

        if (!planRef.current) {
            log.error('Plan not loaded');
            return
        }

        // Optimistically apply the update to the local state
        const updatedPlan = applyLocalUpdate(planRef.current, updateMessage);
        if (!updatedPlan) {
            log.error('Local update failed');
            return;
        }
        planRef.current = updatedPlan;
        setPlan(updatedPlan);

        // Send the update via WebSocket
        pendingUpdatesRef.current.push({ updateId, updateMessage });
        webSocketServiceRef.current?.sendUpdate(updateMessage, updateId);
    };

    // Handle drag and drop
    const onDragEnd = (result: DropResult) => {
        if (!result.destination || !plan) {
            return;
        }

        const sourceIndex = result.source.index;
        const destinationIndex = result.destination.index;

        if (sourceIndex === destinationIndex) {
            return;
        }

        const movedPlace = plan.places[sourceIndex];
        const targetPlace = plan.places[destinationIndex];

        // Send update to the server via WebSocket
        const updateMessage: PlanUpdateMessage = {
            action: 'REORDER',
            placeId: movedPlace.placeId,
            targetPlaceId: targetPlace.placeId,
            clientId: webSocketServiceRef.current?.getClientId() || '',
            updateId: uuidv4(),
            version: plan.version,
        };

        sendUpdate(updateMessage);
    };

    if (!plan || !plan.places) {
        return <div>Loading...</div>;
    }

    const handleAdd = async (e: React.FormEvent) => {
        e.preventDefault();

        // Send update to the server via WebSocket
        const updateMessage: PlanUpdateMessage = {
            clientId: webSocketServiceRef.current?.getClientId() || '',
            updateId: uuidv4(),
            action: 'ADD',
            placeId: uuidv4(),
            version: plan.version,
            googlePlaceId: newGooglePlaceId,
            staySeconds: 1800,
        };

        sendUpdate(updateMessage);

        setNewGooglePlaceId('');
    };

    const handleDelete = async (placeId: string) => {
        const index = plan.places.findIndex(place => place.placeId === placeId);
        if (index === -1) {
            log.error('Place not found:', placeId);
            return;
        }

        // Send update to the server via WebSocket
        const updateMessage: PlanUpdateMessage = {
            clientId: webSocketServiceRef.current?.getClientId() || '',
            updateId: uuidv4(),
            action: 'REMOVE',
            placeId,
            version: plan.version,
        };

        sendUpdate(updateMessage);
    };

    const handleStaySecondsUpdate = async (placeId: string, newStaySeconds: number) => {
        const place = plan.places.find(place => place.placeId === placeId);
        if (!place) {
            log.error('Place not found:', placeId);
            return;
        }

        // Send update to the server via WebSocket
        const updateMessage: PlanUpdateMessage = {
            clientId: webSocketServiceRef.current?.getClientId() || '',
            updateId: uuidv4(),
            action: 'UPDATE',
            placeId,
            version: plan.version,
            googlePlaceId: place.googlePlaceId,
            staySeconds: newStaySeconds,
        };

        sendUpdate(updateMessage);
    }

    return (
        <>
            <form onSubmit={handleAdd}>
                <div>
                    <label htmlFor="newGooglePlaceId">New Google Place Id:</label>
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
                        <ul {...provided.droppableProps} ref={provided.innerRef}>
                            {plan.places.map((place, index) => (
                                <Draggable key={place.placeId} draggableId={place.placeId} index={index}>
                                    {(provided) => (
                                        <li
                                            ref={provided.innerRef}
                                            {...provided.draggableProps}
                                            {...provided.dragHandleProps}
                                        >
                                            {place.placeId}{' '}{place.googlePlaceId}{' '}{place.staySeconds}{' '}
                                            <StaySecondsEditor
                                                placeId={place.placeId}
                                                currentStaySeconds={place.staySeconds}
                                                onSubmit={handleStaySecondsUpdate}
                                            />
                                            <button onClick={() => handleDelete(place.placeId)}>Delete</button>
                                        </li>
                                    )}
                                </Draggable>
                            ))}
                            {provided.placeholder}
                        </ul>
                    )}
                </Droppable>
            </DragDropContext>
        </>
    );
};

export default PlanComponent;
