package org.ethanhao.triprover.service;

import java.util.List;

import org.ethanhao.triprover.domain.PlanMember;
import org.ethanhao.triprover.dto.plan.PlanBaseDTO;
import org.ethanhao.triprover.dto.plan.PlanPlacesResponseDTO;
import org.ethanhao.triprover.dto.plan.PlanSummaryResponseDTO;
import org.ethanhao.triprover.dto.plan.PlanUpdateDTO;
import org.ethanhao.triprover.dto.plan.member.BatchPlanMemberAdditionResponseDTO;
import org.ethanhao.triprover.dto.plan.member.PlanMemberBaseDTO;
import org.ethanhao.triprover.dto.plan.member.PlanMemberResponseDTO;
import org.ethanhao.triprover.dto.plan.member.PlanMemberUpdateDTO;

public interface PlanService {
    List<PlanSummaryResponseDTO> getPlanSummaries(Long userId);

    PlanSummaryResponseDTO getPlanSummary(Long userId, Long planId);

    PlanSummaryResponseDTO createPlan(Long userId, PlanBaseDTO request);

    void deletePlan(Long planId);

    PlanSummaryResponseDTO updatePlan(Long userId, Long planId, PlanUpdateDTO request);

    List<PlanMemberResponseDTO> getPlanMembers(Long planId);

    BatchPlanMemberAdditionResponseDTO addPlanMembers(Long userId, Long planId,
            List<PlanMemberUpdateDTO> requests);

    void removePlanMember(Long userId, Long planId, PlanMemberBaseDTO request);

    PlanMemberResponseDTO updatePlanMemberRole(Long userId, Long planId, PlanMemberUpdateDTO request);

    PlanPlacesResponseDTO getPlanPlaces(Long planId);

    boolean hasRole(Long userId, Long planId, PlanMember.RoleType requiredRole);
}
