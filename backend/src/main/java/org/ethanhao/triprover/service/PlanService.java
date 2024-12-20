package org.ethanhao.triprover.service;

import java.util.List;

import org.ethanhao.triprover.domain.PlanMember;
import org.ethanhao.triprover.dto.PlanCreation;
import org.ethanhao.triprover.dto.PlanMemberDelete;
import org.ethanhao.triprover.dto.PlanMemberUpdate;
import org.ethanhao.triprover.dto.PlanPlaces;
import org.ethanhao.triprover.dto.PlanSummary;
import org.ethanhao.triprover.dto.PlanUpdate;

public interface PlanService {
    List<PlanSummary> getPlanSummaries(Long userId);
    PlanSummary getPlanSummary(Long userId, Long planId);
    PlanSummary createPlan(Long userId, PlanCreation request);
    void deletePlan(Long planId);
    PlanSummary updatePlan(Long userId, Long planId, PlanUpdate request);
    PlanSummary addPlanMember(Long userId, Long planId, PlanMemberUpdate request);
    PlanSummary removePlanMember(Long userId, Long planId, PlanMemberDelete request);
    PlanSummary updatePlanMemberRole(Long userId, Long planId, PlanMemberUpdate request);
    PlanPlaces getPlanPlaces(Long planId);
    boolean hasRole(Long userId,Long planId, PlanMember.RoleType requiredRole);
}
