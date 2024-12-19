package org.ethanhao.triprover.dto;

import org.ethanhao.triprover.domain.PlanMember;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlanMemberUpdate {

    @NotBlank(message = "User name is required")
    private String userName;
    
    @NotNull(message = "Plan ID is required")
    private Long planId;

    @NotNull(message = "Role is required")
    private PlanMember.RoleType role;
} 