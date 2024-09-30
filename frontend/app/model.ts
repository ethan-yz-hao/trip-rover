// models.ts

export interface Place {
    placeId: string;
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
    placeId?: string;        // For ADD action
    index?: number;          // For REMOVE action
    fromIndex?: number;      // For REORDER action
    toIndex?: number;        // For REORDER action
    // Additional fields as needed
}