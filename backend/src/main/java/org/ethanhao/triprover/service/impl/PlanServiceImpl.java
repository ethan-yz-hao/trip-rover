package org.ethanhao.triprover.service.impl;

import org.ethanhao.triprover.domain.Plan;
import org.ethanhao.triprover.domain.PlanUpdateMessage;
import org.ethanhao.triprover.handler.ResourceNotFoundException;
import org.ethanhao.triprover.repository.PlanRepository;
import org.ethanhao.triprover.service.PlanService;
import org.ethanhao.triprover.service.PlanUpdateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class PlanServiceImpl implements PlanService {

    private final PlanRepository planRepository;

    private final PlanUpdateService planUpdateService;

    @Autowired
    public PlanServiceImpl(PlanRepository planRepository, PlanUpdateService planUpdateService) {
        this.planRepository = planRepository;
        this.planUpdateService = planUpdateService;
    }

    @Override
    public Plan getPlan(Long planId) {
        return planRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("Plan not found with ID: " + planId));
    }

    @Override
    public boolean isUserAuthorized(Long planId, Long userId) {
        return planRepository.existsByPlanIdAndUsers_Id(planId, userId);
    }

    @Override
    public Long applyUpdate(Long planId, PlanUpdateMessage updateMessage) {
        return planUpdateService.updatePlanWithMessage(planId, updateMessage);
    }
}