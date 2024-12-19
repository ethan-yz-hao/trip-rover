package org.ethanhao.triprover.service;

import java.util.List;

import org.ethanhao.triprover.domain.PlanMember;
import org.ethanhao.triprover.dto.PlanCreation;
import org.ethanhao.triprover.dto.PlanPlaces;
import org.ethanhao.triprover.dto.PlanSummary;

public interface PlanService {
    List<PlanSummary> getUserPlans(Long userId);
    PlanSummary createPlan(Long userId, PlanCreation request);
    PlanPlaces getPlanPlaces(Long planId);
    boolean hasRole(Long planId, Long userId, PlanMember.RoleType requiredRole);
}
