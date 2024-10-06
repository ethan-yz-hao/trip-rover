package org.ethanhao.triprover.service.impl;

import org.ethanhao.triprover.domain.Plan;
import org.ethanhao.triprover.domain.PlanUpdateMessage;
import org.ethanhao.triprover.domain.PlanUserRole;
import org.ethanhao.triprover.handler.ResourceNotFoundException;
import org.ethanhao.triprover.repository.PlanRepository;
import org.ethanhao.triprover.repository.PlanUserRoleRepository;
import org.ethanhao.triprover.service.PlanService;
import org.ethanhao.triprover.service.PlanUpdateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class PlanServiceImpl implements PlanService {

    private final PlanRepository planRepository;

    private final PlanUpdateService planUpdateService;

    private final PlanUserRoleRepository planUserRoleRepository;

    @Autowired
    public PlanServiceImpl(PlanRepository planRepository, PlanUpdateService planUpdateService, PlanUserRoleRepository planUserRoleRepository) {
        this.planRepository = planRepository;
        this.planUpdateService = planUpdateService;
        this.planUserRoleRepository = planUserRoleRepository;
    }

    @Override
    public Plan getPlan(Long planId) {
        return planRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("Plan not found with ID: " + planId));
    }

    @Override
    public boolean hasRole(Long planId, Long userId, PlanUserRole.RoleType requiredRole) {
        PlanUserRole planUserRole = planUserRoleRepository.findByIdPlanPlanIdAndIdUserId(planId, userId);
        if (planUserRole == null) {
            return false;
        }
        // Check if the user's role meets or exceeds the required role
        return planUserRole.getRole().ordinal() <= requiredRole.ordinal();
    }

    @Override
    public Long applyUpdate(Long planId, PlanUpdateMessage updateMessage) {
        return planUpdateService.updatePlanWithMessage(planId, updateMessage);
    }
}