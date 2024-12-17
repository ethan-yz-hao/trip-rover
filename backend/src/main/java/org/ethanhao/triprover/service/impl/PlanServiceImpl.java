package org.ethanhao.triprover.service.impl;

import org.ethanhao.triprover.domain.PlanUpdateMessage;
import org.ethanhao.triprover.domain.PlanMember;
import org.ethanhao.triprover.dto.PlanPlaces;
import org.ethanhao.triprover.handler.ResourceNotFoundException;
import org.ethanhao.triprover.repository.PlanRepository;
import org.ethanhao.triprover.repository.PlanMemberRepository;
import org.ethanhao.triprover.service.PlanService;
import org.ethanhao.triprover.service.PlanUpdateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class PlanServiceImpl implements PlanService {

    private final PlanRepository planRepository;

    private final PlanUpdateService planUpdateService;

    private final PlanMemberRepository planMemberRepository;

    @Autowired
    public PlanServiceImpl(PlanRepository planRepository, PlanUpdateService planUpdateService, PlanMemberRepository planMemberRepository) {
        this.planRepository = planRepository;
        this.planUpdateService = planUpdateService;
        this.planMemberRepository = planMemberRepository;
    }

    @Override
    public PlanPlaces getPlanPlaces(Long planId) {
        return PlanPlaces.fromEntity(planRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("Plan not found with ID: " + planId)));
    }

    @Override
    public boolean hasRole(Long planId, Long userId, PlanMember.RoleType requiredRole) {
        PlanMember planMember = planMemberRepository.findByIdPlanPlanIdAndIdUserId(planId, userId);
        if (planMember == null) {
            return false;
        }
        // Check if the user's role meets or exceeds the required role
        return planMember.getRole().ordinal() <= requiredRole.ordinal();
    }

    @Override
    public Long applyUpdate(Long planId, PlanUpdateMessage updateMessage) {
        return planUpdateService.updatePlanWithMessage(planId, updateMessage);
    }
}