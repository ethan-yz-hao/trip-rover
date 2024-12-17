package org.ethanhao.triprover.dto;

import lombok.Data;
import org.ethanhao.triprover.domain.Plan;
import org.ethanhao.triprover.domain.PlanPlace;

import java.util.List;

@Data
public class PlanDTO {
    private Long planId;
    private String planName;
    private Long version;
    private List<PlanPlace> places;

    // Constructor to convert from Plan entity
    public static PlanDTO fromEntity(Plan plan) {
        PlanDTO dto = new PlanDTO();
        dto.setPlanId(plan.getPlanId());
        dto.setPlanName(plan.getPlanName());
        dto.setVersion(plan.getVersion());
        dto.setPlaces(plan.getPlaces());
        return dto;
    }

    // Method to convert back to Plan entity
    public Plan toEntity() {
        Plan plan = new Plan();
        plan.setPlanId(this.planId);
        plan.setPlanName(this.planName);
        plan.setVersion(this.version);
        plan.setPlaces(this.places);
        return plan;
    }
} 