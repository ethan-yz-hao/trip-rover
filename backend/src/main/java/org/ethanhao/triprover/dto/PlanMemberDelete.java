package org.ethanhao.triprover.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlanMemberDelete {

    @NotBlank(message = "User name is required")
    private String userName;
    
    @NotNull(message = "Plan ID is required")
    private Long planId;
} 