package org.ethanhao.triprover.service.impl;

import org.ethanhao.triprover.dto.PostPlan;
import org.ethanhao.triprover.dto.GetPlan;
import org.ethanhao.triprover.domain.Plan;
import org.ethanhao.triprover.domain.PlanUserRole;
import org.ethanhao.triprover.domain.PlanUserRoleId;
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
    public List<GetPlan> getUserPlans(Long userId) {
        
        List<GetPlan> planDTOs = planRepository.findPlansWithRolesByUserId(userId);
        logger.info("Fetched {} plans for user ID: {}", planDTOs.size(), userId);

        return planDTOs;
    }

    @Transactional
    @Override
    public GetPlan createPlan(Long userId, PostPlan request) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException(userId));

        Plan plan = new Plan();
        plan.setPlanName(request.getPlanName());
        plan.setPlanUserRoles(new HashSet<>());
        
        // Create PlanUserRole with OWNER role
        PlanUserRole planUserRole = new PlanUserRole();
        planUserRole.setId(new PlanUserRoleId(plan, user));
        planUserRole.setRole(PlanUserRole.RoleType.OWNER);
        
        plan.getPlanUserRoles().add(planUserRole);
        
        Plan savedPlan = planRepository.save(plan);
        
        // Use the same query method to return the DTO
        return planRepository.findPlansWithRolesByUserId(userId).stream()
            .filter(p -> p.getPlanId().equals(savedPlan.getPlanId()))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Failed to retrieve created plan"));
    }
} 