// models.ts

export interface Place {
    placeId: string;
    googlePlaceId: string;
    staySeconds: number;
}

export interface Plan {
    planId: number;
    planName: string;
    version: number;
    places: Place[];
}

export interface PlanUpdateMessage {
    clientId: string;
    updateId: string;
    action: 'ADD' | 'REMOVE' | 'REORDER';
    placeId: string;              // For ADD and REMOVE actions
    targetPlaceId?: string;       // For REORDER action
    version: number;
}

export interface PlanAckMessage {
    updateId: string;
    status: 'OK' | 'ERROR';
    errorMessage?: string;
}