'use client';
import React, {useState, useEffect, useRef} from 'react';
import {Plan, PlanUpdateMessage} from '@/app/model';
import {DragDropContext, Droppable, Draggable, DropResult} from '@hello-pangea/dnd';
import WebSocketService from '@/app/webSocketService';
import {v4 as uuidv4} from 'uuid';

interface PlanComponentProps {
    planId: number;
}

const PlanComponent: React.FC<PlanComponentProps> = ({planId}) => {
    const backendUrl = process.env.NEXT_PUBLIC_BACKEND_URL;
    const [plan, setPlan] = useState<Plan | null>(null);
    const planRef = useRef<Plan | null>(null);
    const webSocketServiceRef = useRef<WebSocketService | null>(null);
    const [newPlaceId, setNewPlaceId] = useState<string>('');

    // Track pending optimistic updates
    const pendingUpdatesRef = useRef<Map<string, PlanUpdateMessage>>(new Map());

    // Load the plan data
    useEffect(() => {
        // Fetch plan data from the server
        fetch(`${backendUrl}/api/plan/${planId}`, {
            credentials: 'include', // Include cookies in the request
        })
            .then(response => {
                if (!response.ok) {
                    throw new Error('Failed to fetch plan');
                }
                return response.json();
            })
            .then((data: Plan) => setPlan(data))
            .catch(error => console.error('Error fetching plan:', error));
    }, [planId, backendUrl]);

    // Store the plan data in a ref
    useEffect(() => {
        if (plan) {
            planRef.current = plan;
        }
    }, [plan]);

    // Initialize and manage WebSocket connection
    useEffect(() => {
        if (webSocketServiceRef.current) return;  // avoid re-initializing

        const webSocketService = new WebSocketService(planId);
        webSocketServiceRef.current = webSocketService;

        webSocketService.connect((updateMessage: PlanUpdateMessage) => {
            console.log('WebSocket message received:', updateMessage);
            handleUpdateMessage(updateMessage);
        });

        return () => {
            webSocketService.disconnect();
        };
    }, [planId]);

    const handleUpdateMessage = (updateMessage: PlanUpdateMessage) => {
        const plan = planRef.current;
        if (!plan) {
            console.error('Plan not loaded');
            return;
        }

        const {clientId, updateId} = updateMessage;

        // If the update originated from this client, remove it from pending updates and do not re-apply
        if (clientId === webSocketServiceRef.current?.getClientId()) {
            pendingUpdatesRef.current.delete(updateId);
            return;
        }

        switch (updateMessage.action) {
            case 'REORDER':
                const {fromIndex, toIndex} = updateMessage;
                if (fromIndex === undefined || toIndex === undefined) return;

                const reorderedPlaces = Array.from(plan.places);
                const [movedPlace] = reorderedPlaces.splice(fromIndex, 1);
                reorderedPlaces.splice(toIndex, 0, movedPlace);

                setPlan({...plan, places: reorderedPlaces});
                break;

            case 'ADD':
                if (updateMessage.placeId) {
                    setPlan({...plan, places: [...plan.places, {placeId: updateMessage.placeId}]});
                }
                break;

            case 'REMOVE':
                if (updateMessage.index !== undefined) {
                    const updatedPlaces = plan.places.filter((_, idx) => idx !== updateMessage.index);
                    setPlan({...plan, places: updatedPlaces});
                }
                break;

            default:
                console.error('Unknown action:', updateMessage.action);
        }
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

        const updatedPlaces = Array.from(plan.places);
        const [movedPlace] = updatedPlaces.splice(sourceIndex, 1);
        updatedPlaces.splice(destinationIndex, 0, movedPlace);

        setPlan({...plan, places: updatedPlaces});

        // Send update to the server via WebSocket
        const updateMessage: PlanUpdateMessage = {
            clientId: webSocketServiceRef.current?.getClientId() || '',
            updateId: uuidv4(),
            action: 'REORDER',
            fromIndex: sourceIndex,
            toIndex: destinationIndex,
        };

        const updateId = webSocketServiceRef.current?.sendUpdate(updateMessage);
        if (updateId) {
            // Track the pending optimistic update
            pendingUpdatesRef.current.set(updateId, updateMessage);
        }
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
        };

        const updateId = webSocketServiceRef.current?.sendUpdate(updateMessage);
        if (updateId) {
            // Track the pending optimistic update
            pendingUpdatesRef.current.set(updateId, updateMessage);

            // Optimistically update the UI
            setPlan({...plan, places: [...plan.places, {placeId: newPlaceId}]});
            setNewPlaceId('');
        }
    };

    const handleDelete = async (placeId: string) => {
        const index = plan.places.findIndex(place => place.placeId === placeId);
        if (index === -1) {
            console.error('Place not found:', placeId);
            return;
        }

        // Send update to the server via WebSocket
        const updateMessage: PlanUpdateMessage = {
            clientId: webSocketServiceRef.current?.getClientId() || '',
            updateId: uuidv4(),
            action: 'REMOVE',
            index,
        };

        const updateId = webSocketServiceRef.current?.sendUpdate(updateMessage);
        if (updateId) {
            // Track the pending optimistic update
            pendingUpdatesRef.current.set(updateId, updateMessage);

            // Optimistically update the UI
            const updatedPlaces = plan.places.filter((_, idx) => idx !== index);
            setPlan({...plan, places: updatedPlaces});
        }
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
