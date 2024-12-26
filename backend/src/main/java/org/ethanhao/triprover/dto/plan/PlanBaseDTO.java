package org.ethanhao.triprover.dto.plan;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PlanBaseDTO {
    @NotBlank(message = "Plan name is required")
    private String planName;

    private Boolean isPublic;

    private String description;
} 