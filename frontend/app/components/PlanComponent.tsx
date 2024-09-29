'use client';
import React, { useState, useEffect } from 'react';
import {Plan} from "@/app/model";
import { DragDropContext, Droppable, Draggable, DropResult } from '@hello-pangea/dnd';

interface PlanComponentProps {
    planId: number;
}

const PlanComponent: React.FC<PlanComponentProps> = ({ planId }) => {
    const backendUrl = process.env.NEXT_PUBLIC_BACKEND_URL;
    const [plan, setPlan] = useState<Plan | null>(null);

    // Load the plan data
    useEffect(() => {
        // Fetch plan data from the server
        fetch(`${backendUrl}/api/plan/${planId}`, {
            credentials: 'include', // Include cookies in the request
        })
            .then(response => response.json())
            .then(data => setPlan(data))
            .catch(error => console.error('Error fetching plan:', error));
    }, [planId]);

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

        setPlan({ ...plan, places: updatedPlaces });

        // // Send update to the server via WebSocket
        // const updateMessage: PlanUpdateMessage = {
        //     action: 'REORDER',
        //     fromIndex: sourceIndex,
        //     toIndex: destinationIndex,
        // };
        //
        // webSocketService.sendUpdate(planId, updateMessage);
    };

    if (!plan || !plan.places) {
        return <div>Loading...</div>;
    }

    return (
        <DragDropContext onDragEnd={onDragEnd}>
            <Droppable droppableId="places">
                {(provided) => (
                    <ul {...provided.droppableProps} ref={provided.innerRef}>
                        {plan.places.map((Place, index) => (
                            <Draggable key={Place.placeId} draggableId={Place.placeId} index={index}>
                                {(provided) => (
                                    <li
                                        ref={provided.innerRef}
                                        {...provided.draggableProps}
                                        {...provided.dragHandleProps}
                                    >
                                        {Place.placeId} {Place.name}
                                    </li>
                                )}
                            </Draggable>
                        ))}
                        {provided.placeholder}
                    </ul>
                )}
            </Droppable>
        </DragDropContext>
    );
};

export default PlanComponent;
