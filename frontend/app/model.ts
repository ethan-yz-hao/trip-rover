export interface Place {
    placeId: string;
    googlePlaceId: string;
    staySeconds: number;
}

export interface PlanPlaces {
    planId: number;
    planName: string;
    version: number;
    places: Place[];
}

export interface PlanSummary {
    planId: number;
    planName: string;
    role: 'OWNER' | 'EDITOR' | 'VIEWER';
    createTime: Date;
    updateTime: Date;
}

export interface PlanUpdateMessage {
    clientId: string;
    updateId: string;
    action: 'ADD' | 'REMOVE' | 'REORDER' | 'UPDATE';
    placeId: string;              // For ADD and REMOVE actions
    targetPlaceId?: string;       // For REORDER action
    version: number;
    googlePlaceId?: string;       // For ADD and UPDATE actions
    staySeconds?: number;         // For ADD and UPDATE actions
}

export interface PlanAckMessage {
    updateId: string;
    status: 'OK' | 'ERROR';
    errorMessage?: string;
}

export interface ResponseResult<T> {
    code: number;
    msg: string;
    data: T;
}

export interface UserIndexResponseDTO {
    id: number;
    userName: string;
    nickName: string;
}

export interface PlanIndexResponseDTO {
    planId: number;
    planName: string;
}