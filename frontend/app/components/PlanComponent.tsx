'use client';
import React, {useState, useEffect, useRef} from 'react';
import {Place, Plan, PlanAckMessage, PlanUpdateMessage} from '@/app/model';
import {DragDropContext, Droppable, Draggable, DropResult} from '@hello-pangea/dnd';
import WebSocketService from '@/app/webSocketService';
import {v4 as uuidv4} from 'uuid';
import axios from "axios";
import log from "@/app/log";

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
    const [newPlaceId, setNewPlaceId] = useState<string>('');

    // Ordered queue of pending updates
    const pendingUpdatesRef = useRef<PendingUpdate[]>([]);

    // fetch the plan data
    const fetchPlanData = async () => {
        try {
            const response = await axios.get<Plan>(`${backendUrl}/api/plan/${planId}`, {
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
        setPlan(prevPlan => {
            if (!prevPlan) return prevPlan;

            const updatedPlan = applyUpdate(prevPlan, updateMessage);
            updatedPlan.version = version
            planRef.current = updatedPlan;
            return updatedPlan;
        });
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

    // Optimistically apply updates to the local state
    const applyUpdateLocally = (updateMessage: PlanUpdateMessage) => {
        setPlan(prevPlan => {
            if (!prevPlan) return prevPlan;

            const updatedPlan = applyUpdate(prevPlan, updateMessage);
            planRef.current = updatedPlan;
            return updatedPlan;
        });
    };

    // Apply update to the plan
    const applyUpdate = (currentPlan: Plan, updateMessage: PlanUpdateMessage): Plan => {
        switch (updateMessage.action) {
            case 'REORDER':
                const { placeId, targetPlaceId } = updateMessage;
                const placeToMoveIndex = currentPlan.places.findIndex(place => place.placeId === placeId);
                if (placeToMoveIndex === -1) {
                    log.error(`Place with ID ${placeId} not found for REORDER`);
                    return currentPlan;
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
                if (updateMessage.placeId) {
                    const newPlace: Place = {
                        placeId: updateMessage.placeId,
                    };
                    return { ...currentPlan, places: [...currentPlan.places, newPlace] };
                }
                return currentPlan;

            case 'REMOVE':
                if (updateMessage.placeId) {
                    const filteredPlaces = currentPlan.places.filter(place => place.placeId !== updateMessage.placeId);
                    return { ...currentPlan, places: filteredPlaces };
                }
                return currentPlan;

            default:
                log.error('Unknown action:', updateMessage.action);
                return currentPlan;
        }
    };

    // Send an update message
    const sendUpdate = (updateMessage: PlanUpdateMessage) => {
        const updateId = uuidv4();
        pendingUpdatesRef.current.push({ updateId, updateMessage });

        // Optimistically apply the update to the local state
        applyUpdateLocally(updateMessage);

        // Send the update via WebSocket
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

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();

        // Send update to the server via WebSocket
        const updateMessage: PlanUpdateMessage = {
            clientId: webSocketServiceRef.current?.getClientId() || '',
            updateId: uuidv4(),
            action: 'ADD',
            placeId: newPlaceId,
            version: plan.version,
        };

        sendUpdate(updateMessage);

        setNewPlaceId('');
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

    return (
        <>
            <form onSubmit={handleSubmit}>
                <div>
                    <label htmlFor="newPlaceId">New Place Index:</label>
                    <input
                        type="text"
                        id="newPlaceId"
                        value={newPlaceId}
                        onChange={(e) => setNewPlaceId(e.target.value)}
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
                                            {place.placeId}{' '}
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
