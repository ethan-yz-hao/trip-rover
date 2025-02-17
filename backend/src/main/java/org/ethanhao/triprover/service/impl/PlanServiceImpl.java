package org.ethanhao.triprover.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.ethanhao.triprover.domain.Plan;
import org.ethanhao.triprover.domain.PlanMember;
import org.ethanhao.triprover.domain.PlanMemberId;
import org.ethanhao.triprover.domain.User;
import org.ethanhao.triprover.dto.plan.PlanBaseDTO;
import org.ethanhao.triprover.dto.plan.PlanPlacesResponseDTO;
import org.ethanhao.triprover.dto.plan.PlanSummaryResponseDTO;
import org.ethanhao.triprover.dto.plan.PlanUpdateDTO;
import org.ethanhao.triprover.dto.plan.member.PlanMemberBaseDTO;
import org.ethanhao.triprover.dto.plan.member.PlanMemberUpdateDTO;
import org.ethanhao.triprover.exception.PlanOperationException;
import org.ethanhao.triprover.mapper.PlanMapper;
import org.ethanhao.triprover.mapper.PlanMemberMapper;
import org.ethanhao.triprover.repository.PlanMemberRepository;
import org.ethanhao.triprover.repository.PlanRepository;
import org.ethanhao.triprover.repository.UserRepository;
import org.ethanhao.triprover.service.PlanService;
import org.ethanhao.triprover.service.PlanUpdateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.ResourceAccessException;

@Service
public class PlanServiceImpl implements PlanService {

    private final PlanRepository planRepository;

    private final PlanMemberRepository planMemberRepository;

    private final UserRepository userRepository;

    private final PlanMapper planMapper;

    private final PlanMemberMapper planMemberMapper;

    @Autowired
    public PlanServiceImpl(PlanRepository planRepository, PlanUpdateService planUpdateService,
            PlanMemberRepository planMemberRepository, UserRepository userRepository, PlanMapper planMapper,
            PlanMemberMapper planMemberMapper) {
        this.planRepository = planRepository;
        this.planMemberRepository = planMemberRepository;
        this.userRepository = userRepository;
        this.planMapper = planMapper;
        this.planMemberMapper = planMemberMapper;
    }

    @Transactional(readOnly = true)
    @Override
    public List<PlanSummaryResponseDTO> getPlanSummaries(Long userId) {
        List<PlanSummaryResponseDTO> planSummaries = planRepository.findPlanSummaryByUserId(userId);
        return planSummaries;
    }

    @Transactional(readOnly = true)
    @Override
    public PlanSummaryResponseDTO getPlanSummary(Long userId, Long planId) {
        return planRepository.findPlanSummaryByUserIdAndPlanId(userId, planId);
    }

    @Transactional
    @Override
    public PlanSummaryResponseDTO createPlan(Long userId, PlanBaseDTO request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with ID: " + userId));

        Plan plan = planMapper.planBaseDtoToPlan(request);

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
    public PlanSummaryResponseDTO updatePlan(Long userId, Long planId, PlanUpdateDTO request) {
        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new ResourceAccessException("Plan not found with ID: " + planId));

        planMapper.updatePlanFromDto(request, plan);
        planRepository.save(plan);

        return planRepository.findPlanSummaryByUserIdAndPlanId(userId, planId);
    }

    @Transactional(readOnly = true)
    @Override
    public List<PlanMemberBaseDTO> getPlanMembers(Long planId) {
        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new ResourceAccessException("Plan not found with ID: " + planId));

        return plan.getPlanMembers().stream()
                .map(member -> {
                    PlanMemberBaseDTO dto = new PlanMemberBaseDTO();
                    dto.setPlanId(planId);
                    dto.setUserId(member.getId().getUser().getId());
                    dto.setRole(member.getRole());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public PlanSummaryResponseDTO addPlanMember(Long userId, PlanMemberUpdateDTO request) {
        PlanMember planMember = planMemberMapper.planMemberDTOToPlanMember(request);

        User targetUser = userRepository.findById(planMember.getId().getUser().getId())
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found with ID: " + planMember.getId().getUser().getId()));

        Long planId = planMember.getId().getPlan().getPlanId();

        planRepository.findById(planId)
                .orElseThrow(() -> new ResourceAccessException("Plan not found with ID: " + planId));

        if (targetUser.getId().equals(userId)) {
            throw new PlanOperationException("Cannot update member of oneself");
        }

        if (planRepository.findPlanSummaryByUserIdAndPlanId(targetUser.getId(), planId) != null) {
            throw new PlanOperationException("User already added to the plan");
        }

        if (planMember.getRole().ordinal() <= PlanMember.RoleType.OWNER.ordinal()) {
            throw new PlanOperationException("Cannot add owner or above to the plan");
        }

        planMemberRepository.save(planMember);

        return planRepository.findPlanSummaryByUserIdAndPlanId(targetUser.getId(), planId);
    }

    @Transactional
    @Override
    public PlanSummaryResponseDTO removePlanMember(Long userId, PlanMemberBaseDTO request) {

        PlanMemberId planMemberId = planMemberMapper.planMemberIdDTOToPlanMemberId(request);

        PlanMember planMember = planMemberRepository.findById(planMemberId)
                .orElseThrow(() -> new ResourceAccessException("Plan member not found with ID: " + planMemberId));

        if (planMember.getRole().ordinal() <= PlanMember.RoleType.OWNER.ordinal()) {
            throw new PlanOperationException("Cannot remove owner or above from the plan");
        }

        planMemberRepository.deleteById(planMemberId);

        return planRepository.findPlanSummaryByUserIdAndPlanId(
                planMemberId.getUser().getId(),
                planMemberId.getPlan().getPlanId());
    }

    @Transactional
    @Override
    public PlanSummaryResponseDTO updatePlanMemberRole(Long userId, PlanMemberUpdateDTO request) {
        PlanMember planMember = planMemberMapper.planMemberDTOToPlanMember(request);

        PlanMember targetPlanMember = planMemberRepository.findById(planMember.getId())
                .orElseThrow(() -> new ResourceAccessException("Plan member not found with ID: " + planMember.getId()));

        if (targetPlanMember.getId().getUser().getId().equals(userId)) {
            throw new PlanOperationException("Cannot update oneself's role");
        }

        if (planMember.getRole().ordinal() <= PlanMember.RoleType.OWNER.ordinal()) {
            throw new PlanOperationException("Cannot update role to owner or above");
        }

        targetPlanMember.setRole(planMember.getRole());

        planMemberRepository.save(targetPlanMember);

        return planRepository.findPlanSummaryByUserIdAndPlanId(
                planMember.getId().getUser().getId(),
                planMember.getId().getPlan().getPlanId());
    }

    @Transactional(readOnly = true)
    @Override
    public PlanPlacesResponseDTO getPlanPlaces(Long planId) {
        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new ResourceAccessException("Plan not found with ID: " + planId));
        return planMapper.planToPlanPlacesResponseDto(plan);
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