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
        REORDER
    }

    private ActionType action;
    private String placeId;     // For ADD action
    private int index;          // For REMOVE actions
    private int fromIndex;      // For REORDER action
    private int toIndex;        // For REORDER action
}