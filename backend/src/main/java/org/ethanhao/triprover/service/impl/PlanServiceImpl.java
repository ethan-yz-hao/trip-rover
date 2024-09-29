package org.ethanhao.triprover.service.impl;

import org.ethanhao.triprover.domain.Plan;
import org.ethanhao.triprover.domain.PlanPlace;
import org.ethanhao.triprover.handler.ResourceNotFoundException;
import org.ethanhao.triprover.repository.PlanRepository;
import org.ethanhao.triprover.service.PlanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PlanServiceImpl implements PlanService {

    private final PlanRepository planRepository;

    @Autowired
    public PlanServiceImpl(PlanRepository planRepository) {
        this.planRepository = planRepository;
    }

    @Override
    public Plan getPlan(Long planId) {
        return planRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("Plan not found with ID: " + planId));
    }

    @Override
    public boolean isUserAuthorized(Long planId, Long userId) {
        // Check if the user is associated with the plan
        Plan plan = getPlan(planId);
        return plan.getUsers().stream().anyMatch(user -> user.getId().equals(userId));
    }

}