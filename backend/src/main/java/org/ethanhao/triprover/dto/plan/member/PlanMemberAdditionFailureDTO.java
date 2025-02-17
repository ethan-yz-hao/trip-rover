package org.ethanhao.triprover.dto.plan.member;

import lombok.Data;
import lombok.Builder;

@Data
@Builder
public class PlanMemberAdditionFailureDTO {
    private Long userId;
    private String error;
}