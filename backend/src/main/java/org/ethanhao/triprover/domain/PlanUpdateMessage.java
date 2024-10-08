package org.ethanhao.triprover.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlanUpdateMessage {

    public enum ActionType {
        ADD,
        REMOVE,
        REORDER,
        UPDATE
    }

    private String clientId;
    private String updateId;
    private ActionType action;
    private String placeId;           // For ADD and REMOVE actions
    private String targetPlaceId;     // For REORDER action: the place after which to move
    private Long version;
    private String googlePlaceId;     // For ADD and UPDATE actions
    private Long staySeconds;         // For ADD and UPDATE actions
}