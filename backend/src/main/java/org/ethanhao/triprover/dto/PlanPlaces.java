package org.ethanhao.triprover.dto;

import lombok.Data;
import org.ethanhao.triprover.domain.Plan;
import org.ethanhao.triprover.domain.PlanPlace;

import java.util.List;

@Data
public class PlanPlaces {
    private Long planId;
    private String planName;
    private Long version;
    private List<PlanPlace> places;

    // Constructor to convert from Plan entity
    public static PlanPlaces fromEntity(Plan plan) {
        PlanPlaces dto = new PlanPlaces();
        dto.setPlanId(plan.getPlanId());
        dto.setPlanName(plan.getPlanName());
        dto.setVersion(plan.getVersion());
        dto.setPlaces(plan.getPlaces());
        return dto;
    }
} 