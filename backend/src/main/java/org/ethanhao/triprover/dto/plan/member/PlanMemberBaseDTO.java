package org.ethanhao.triprover.dto.plan.member;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PlanMemberBaseDTO {

    @NotNull(message = "User ID is required")
    private Long userId;
    
    @NotNull(message = "Plan ID is required")
    private Long planId;
} 