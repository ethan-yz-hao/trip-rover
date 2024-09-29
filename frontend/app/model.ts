// models.ts

export interface Place {
    placeId: string;
    name?: string;
    // Other optional fields
}

export interface Plan {
    planId: number;
    planName: string;
    version: number;
    places: Place[];
}

export interface PlanUpdateMessage {
    action: 'ADD' | 'REMOVE' | 'REORDER';
    placeId?: string;
    fromIndex?: number;
    toIndex?: number;
    // Additional fields as needed
}
