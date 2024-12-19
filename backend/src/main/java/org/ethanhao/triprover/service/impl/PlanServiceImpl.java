package org.ethanhao.triprover.service.impl;

import java.util.HashSet;
import java.util.List;

import org.ethanhao.triprover.domain.Plan;
import org.ethanhao.triprover.domain.PlanMember;
import org.ethanhao.triprover.domain.PlanMemberId;
import org.ethanhao.triprover.domain.User;
import org.ethanhao.triprover.dto.PlanCreation;
import org.ethanhao.triprover.dto.PlanPlaces;
import org.ethanhao.triprover.dto.PlanSummary;
import org.ethanhao.triprover.handler.ResourceNotFoundException;
import org.ethanhao.triprover.handler.UserNotFoundException;
import org.ethanhao.triprover.repository.PlanMemberRepository;
import org.ethanhao.triprover.repository.PlanRepository;
import org.ethanhao.triprover.repository.UserRepository;
import org.ethanhao.triprover.service.PlanService;
import org.ethanhao.triprover.service.PlanUpdateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class PlanServiceImpl implements PlanService {

    private static final Logger logger = LoggerFactory.getLogger(PlanServiceImpl.class);

    private final PlanRepository planRepository;

    private final PlanMemberRepository planMemberRepository;

    private final UserRepository userRepository;

    @Autowired
    public PlanServiceImpl(PlanRepository planRepository, PlanUpdateService planUpdateService, PlanMemberRepository planMemberRepository, UserRepository userRepository) {
        this.planRepository = planRepository;
        this.planMemberRepository = planMemberRepository;
        this.userRepository = userRepository;
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

    @Transactional(readOnly = true)
    @Override
    public List<PlanSummary> getUserPlans(Long userId) {
        
        List<PlanSummary> planSummaries = planRepository.findPlanSummaryByUserId(userId);
        logger.info("Fetched {} plans for user ID: {}", planSummaries.size(), userId);

        return planSummaries;
    }

    @Transactional
    @Override
    public PlanSummary createPlan(Long userId, PlanCreation request) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException(userId));

        Plan plan = new Plan();
        plan.setPlanName(request.getPlanName());
        plan.setPlanMembers(new HashSet<>());
        
        // Create PlanMember with OWNER role
        PlanMember planMember = new PlanMember();
        planMember.setId(new PlanMemberId(plan, user));
        planMember.setRole(PlanMember.RoleType.OWNER);
        
        plan.getPlanMembers().add(planMember);
        
        Plan savedPlan = planRepository.save(plan);
        
        // Use the same query method to return the DTO
        return planRepository.findPlanSummaryByUserId(userId).stream()
            .filter(p -> p.getPlanId().equals(savedPlan.getPlanId()))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Failed to retrieve created plan"));
    }
}