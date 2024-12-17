package org.ethanhao.triprover.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlanCreation {
    @NotBlank(message = "Plan name is required")
    private String planName;
} 