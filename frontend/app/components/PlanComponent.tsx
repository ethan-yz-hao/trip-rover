'use client';
import React, {useState, useEffect, useRef} from 'react';
import {Plan, PlanUpdateMessage} from '@/app/model';
import {DragDropContext, Droppable, Draggable, DropResult} from '@hello-pangea/dnd';
import WebSocketService from '@/app/webSocketService';

interface PlanComponentProps {
    planId: number;
}

const PlanComponent: React.FC<PlanComponentProps> = ({planId}) => {
    const backendUrl = process.env.NEXT_PUBLIC_BACKEND_URL;
    const [plan, setPlan] = useState<Plan | null>(null);
    const webSocketServiceRef = useRef<WebSocketService | null>(null);
    const [newPlaceId, setNewPlaceId] = useState<string>('');

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

    // Initialize and manage WebSocket connection
    useEffect(() => {
        const webSocketService = new WebSocketService(planId);
        webSocketServiceRef.current = webSocketService;

        webSocketService.connect((updateMessage: PlanUpdateMessage) => {
            handleUpdateMessage(updateMessage);
        });

        return () => {
            webSocketService.disconnect();
        };
    }, [planId]);

    const handleUpdateMessage = (updateMessage: PlanUpdateMessage) => {
        if (!plan) return;

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
            action: 'REORDER',
            fromIndex: sourceIndex,
            toIndex: destinationIndex,
        };

        webSocketServiceRef.current?.sendUpdate(updateMessage);
    };

    if (!plan || !plan.places) {
        return <div>Loading...</div>;
    }

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();

        setPlan({...plan, places: [...plan.places, {placeId: newPlaceId}]});

        // Send update to the server via WebSocket
        const updateMessage: PlanUpdateMessage = {
            action: 'ADD',
            placeId: newPlaceId,
        };

        webSocketServiceRef.current?.sendUpdate(updateMessage);
    }

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
                                            {place.placeId}
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
