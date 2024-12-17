package org.ethanhao.triprover.service.impl;

import org.ethanhao.triprover.dto.PlanCreation;
import org.ethanhao.triprover.dto.PlanSummary;
import org.ethanhao.triprover.domain.Plan;
import org.ethanhao.triprover.domain.PlanMember;
import org.ethanhao.triprover.domain.PlanMemberId;
import org.ethanhao.triprover.domain.User;
import org.ethanhao.triprover.repository.PlanRepository;
import org.ethanhao.triprover.repository.UserRepository;
import org.ethanhao.triprover.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.HashSet;
import java.util.List;
import org.ethanhao.triprover.handler.UserNotFoundException;

@Service
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {
    
    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired
    private PlanRepository planRepository;

    @Autowired
    private UserRepository userRepository;

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