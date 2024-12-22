package org.ethanhao.triprover.dto.plan;

import lombok.Data;
import lombok.EqualsAndHashCode;

import org.ethanhao.triprover.domain.PlanPlace;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class PlanPlacesResponseDTO extends PlanBaseDTO {
    private Long planId;
    private Long version;
    private List<PlanPlace> places;
} 