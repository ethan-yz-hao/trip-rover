package org.ethanhao.triprover.service.impl;

import jakarta.persistence.OptimisticLockException;
import org.ethanhao.triprover.domain.Plan;
import org.ethanhao.triprover.domain.PlanUpdateMessage;
import org.ethanhao.triprover.handler.ResourceNotFoundException;
import org.ethanhao.triprover.repository.PlanRepository;
import org.ethanhao.triprover.service.PlanService;
import org.ethanhao.triprover.service.PlanUpdateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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
    public void applyUpdate(Long planId, PlanUpdateMessage updateMessage) {
        boolean updated = false;
        int retries = 0;
        int maxRetries = 3;

        while (!updated && retries < maxRetries) {
            try {
                planUpdateService.updatePlanWithMessage(planId, updateMessage);
                updated = true;
            } catch (OptimisticLockException e) {
                retries++;
                if (retries >= maxRetries) {
                    throw e; // Max retries reached, rethrow the exception
                }
                // Else, retry
            }
        }
    }
}