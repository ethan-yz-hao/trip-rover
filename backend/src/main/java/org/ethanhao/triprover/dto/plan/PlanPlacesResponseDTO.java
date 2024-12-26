package org.ethanhao.triprover.dto.plan;

import lombok.Data;

import org.ethanhao.triprover.domain.PlanPlace;

import java.util.List;

@Data
public class PlanPlacesResponseDTO {
    private Long planId;
    private Long version;
    private List<PlanPlace> places;
} 