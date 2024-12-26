package org.ethanhao.triprover.dto.plan;

import lombok.Data;

@Data
public class PlanUpdateDTO {

    private String planName;

    private Boolean isPublic;

    private String description;
} 