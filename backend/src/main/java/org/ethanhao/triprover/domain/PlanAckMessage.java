package org.ethanhao.triprover.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlanAckMessage {

    public enum StatusType {
        OK,
        ERROR
    }

    private String updateId;
    private StatusType status;
    private String errorMessage;
}