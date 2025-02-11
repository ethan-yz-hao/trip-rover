package org.ethanhao.triprover.dto.plan;

import lombok.Data;

@Data
public class PlanIndexResponseDTO {
    private Long planId;
    private String planName;
    private String description;
    private Boolean isPublic;
}
