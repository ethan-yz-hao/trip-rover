package org.ethanhao.triprover.service.impl;

import java.util.HashSet;
import java.util.List;

import org.ethanhao.triprover.domain.Plan;
import org.ethanhao.triprover.domain.PlanMember;
import org.ethanhao.triprover.domain.PlanMemberId;
import org.ethanhao.triprover.domain.User;
import org.ethanhao.triprover.dto.PlanCreation;
import org.ethanhao.triprover.dto.PlanMemberDelete;
import org.ethanhao.triprover.dto.PlanMemberUpdate;
import org.ethanhao.triprover.dto.PlanPlaces;
import org.ethanhao.triprover.dto.PlanSummary;
import org.ethanhao.triprover.dto.PlanUpdate;
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

    @Transactional(readOnly = true)
    @Override
    public List<PlanSummary> getPlanSummaries(Long userId) {
        
        List<PlanSummary> planSummaries = planRepository.findPlanSummaryByUserId(userId);
        logger.info("Fetched {} plans for user ID: {}", planSummaries.size(), userId);

        return planSummaries;
    }

    @Transactional(readOnly = true)
    @Override
    public PlanSummary getPlanSummary(Long userId, Long planId) {
        return planRepository.findPlanSummaryByUserIdAndPlanId(userId, planId);
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
        
        return planRepository.findPlanSummaryByUserIdAndPlanId(userId, savedPlan.getPlanId());
    }

    @Transactional
    @Override
    public void deletePlan(Long planId) {
        planRepository.deleteById(planId);
    }

    @Transactional
    @Override
    public PlanSummary updatePlan(Long userId, Long planId, PlanUpdate request) {
        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("Plan not found with ID: " + planId));
        
        if (request.getPlanName() != null) {
            plan.setPlanName(request.getPlanName());
        }
        
        planRepository.save(plan);

        return planRepository.findPlanSummaryByUserIdAndPlanId(userId, planId);
    }

    @Transactional
    @Override
    public PlanSummary addPlanMember(Long userId, Long planId, PlanMemberUpdate request) {
        Plan targetPlan = planRepository.findById(planId)
        .orElseThrow(() -> new ResourceNotFoundException("Plan not found with ID: " + planId));

        User targetUser = userRepository.findByUserName(request.getUserName());

        if (targetUser == null) {
            throw new UserNotFoundException(request.getUserName());
        }

        if (targetUser.getId().equals(userId)) {
            throw new IllegalArgumentException("Cannot update member of oneself");
        }

        if (planRepository.findPlanSummaryByUserIdAndPlanId(targetUser.getId(), planId) != null) {
            throw new IllegalArgumentException("User already added to the plan");
        }

        if (request.getRole().ordinal() <= PlanMember.RoleType.OWNER.ordinal()) {
            throw new IllegalArgumentException("Cannot add owner or above to the plan");
        }

        PlanMember planMember = new PlanMember();
        planMember.setId(new PlanMemberId(targetPlan, targetUser));
        planMember.setRole(request.getRole());

        planMemberRepository.save(planMember);

        return planRepository.findPlanSummaryByUserIdAndPlanId(targetUser.getId(), planId);
    }

    @Transactional
    @Override
    public PlanSummary removePlanMember(Long userId, Long planId, PlanMemberDelete request) {
        Plan targetPlan = planRepository.findById(planId)
        .orElseThrow(() -> new ResourceNotFoundException("Plan not found with ID: " + planId));

        User targetUser = userRepository.findByUserName(request.getUserName());

        if (targetUser == null) {
            throw new UserNotFoundException(request.getUserName());
        }

        if (targetUser.getId().equals(userId)) {
            throw new IllegalArgumentException("Cannot remove oneself from the plan");
        }

        PlanMember planMember = planMemberRepository.findByIdPlanPlanIdAndIdUserId(planId, targetUser.getId());

        if (planMember == null) {
            throw new IllegalArgumentException("User not exist in the plan");
        }

        if (planMember.getRole().ordinal() <= PlanMember.RoleType.OWNER.ordinal()) {
            throw new IllegalArgumentException("Cannot remove owner or above from the plan");
        }

        planMemberRepository.deleteById(new PlanMemberId(targetPlan, targetUser));

        return planRepository.findPlanSummaryByUserIdAndPlanId(targetUser.getId(), planId);
    }

    @Transactional
    @Override
    public PlanSummary updatePlanMemberRole(Long userId, Long planId, PlanMemberUpdate request) {
        planRepository.findById(planId)
        .orElseThrow(() -> new ResourceNotFoundException("Plan not found with ID: " + planId));

        User targetUser = userRepository.findByUserName(request.getUserName());

        if (targetUser == null) {
            throw new UserNotFoundException(request.getUserName());
        }

        if (targetUser.getId().equals(userId)) {
            throw new IllegalArgumentException("Cannot update oneself's role");
        }

        PlanMember planMember = planMemberRepository.findByIdPlanPlanIdAndIdUserId(planId, targetUser.getId());

        if (planMember == null) {
            throw new IllegalArgumentException("User not exist in the plan");
        }

        if (request.getRole().ordinal() <= PlanMember.RoleType.OWNER.ordinal()) {
            throw new IllegalArgumentException("Cannot update role to owner or above");
        }

        planMember.setRole(request.getRole());

        planMemberRepository.save(planMember);

        return planRepository.findPlanSummaryByUserIdAndPlanId(targetUser.getId(), planId);
    }

    @Override
    public PlanPlaces getPlanPlaces(Long planId) {
        return PlanPlaces.fromEntity(planRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("Plan not found with ID: " + planId)));
    }

    @Override
    public boolean hasRole(Long userId, Long planId, PlanMember.RoleType requiredRole) {
        PlanMember planMember = planMemberRepository.findByIdPlanPlanIdAndIdUserId(planId, userId);
        if (planMember == null) {
            return false;
        }
        // Check if the user's role meets or exceeds the required role
        return planMember.getRole().ordinal() <= requiredRole.ordinal();
    }
}