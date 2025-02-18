package org.ethanhao.triprover.service.impl;

import java.util.ArrayList;
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
import org.ethanhao.triprover.dto.plan.member.BatchPlanMemberAdditionResponseDTO;
import org.ethanhao.triprover.dto.plan.member.PlanMemberAdditionFailureDTO;
import org.ethanhao.triprover.dto.plan.member.PlanMemberBaseDTO;
import org.ethanhao.triprover.dto.plan.member.PlanMemberResponseDTO;
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
    public List<PlanMemberResponseDTO> getPlanMembers(Long planId) {
        List<PlanMember> planMembers = planMemberRepository.findByPlanIdWithUser(planId);
        return planMembers.stream()
                .map(planMemberMapper::planMemberToPlanMemberResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public BatchPlanMemberAdditionResponseDTO addPlanMembers(Long userId, Long planId,
            List<PlanMemberUpdateDTO> requests) {
        List<PlanMemberResponseDTO> successfulAdds = new ArrayList<>();
        List<PlanMemberAdditionFailureDTO> failures = new ArrayList<>();

        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new ResourceAccessException("Plan not found with ID: " + planId));

        for (PlanMemberUpdateDTO request : requests) {
            try {
                // Validate target user exists first
                User targetUser = userRepository.findById(request.getId())
                        .orElseThrow(() -> new UsernameNotFoundException(
                                "User not found with ID: " + request.getId()));

                // Validate business rules
                if (targetUser.getId().equals(userId)) {
                    throw new PlanOperationException("Cannot add oneself as a member");
                }

                if (planMemberRepository.findByPlanIdAndUserIdWithUser(planId, targetUser.getId()) != null) {
                    throw new PlanOperationException("User already added to the plan");
                }

                if (request.getRole().ordinal() <= PlanMember.RoleType.OWNER.ordinal()) {
                    throw new PlanOperationException("Cannot add owner or above to the plan");
                }

                // Create PlanMember with the complete User entity
                PlanMember planMember = new PlanMember();
                PlanMemberId memberId = new PlanMemberId();
                memberId.setPlan(plan);
                memberId.setUser(targetUser);
                planMember.setId(memberId);
                planMember.setRole(request.getRole());

                // Save the member
                planMemberRepository.save(planMember);

                // Fetch the updated member with user data
                PlanMember savedMember = planMemberRepository.findByPlanIdAndUserIdWithUser(planId, targetUser.getId());
                successfulAdds.add(planMemberMapper.planMemberToPlanMemberResponseDTO(savedMember));

            } catch (Exception e) {
                failures.add(PlanMemberAdditionFailureDTO.builder()
                        .id(request.getId())
                        .error(e.getMessage())
                        .build());
            }
        }

        return BatchPlanMemberAdditionResponseDTO.builder()
                .successful(successfulAdds)
                .failed(failures)
                .build();
    }

    @Transactional
    @Override
    public void removePlanMember(Long userId, Long planId, PlanMemberBaseDTO request) {
        PlanMemberId planMemberId = planMemberMapper.planMemberIdDTOToPlanMemberId(request, planId);
        PlanMember targetPlanMember = planMemberRepository.findById(planMemberId)
                .orElseThrow(() -> new ResourceAccessException("Plan member not found"));

        if (targetPlanMember.getRole().ordinal() <= PlanMember.RoleType.OWNER.ordinal()) {
            throw new PlanOperationException("Cannot remove owner or above from the plan");
        }

        planMemberRepository.deleteById(planMemberId);
    }

    @Transactional
    @Override
    public PlanMemberResponseDTO updatePlanMemberRole(Long userId, Long planId, PlanMemberUpdateDTO request) {
        PlanMember targetPlanMember = planMemberRepository.findByPlanIdAndUserIdWithUser(planId, request.getId());

        if (targetPlanMember == null) {
            throw new PlanOperationException("Plan member not found");
        }

        if (request.getRole().ordinal() <= PlanMember.RoleType.OWNER.ordinal()) {
            throw new PlanOperationException("Cannot update role to owner or above");
        }

        if (hasRole(targetPlanMember.getId().getUser().getId(), planId, PlanMember.RoleType.OWNER)) {
            throw new PlanOperationException("Cannot update owner's role");
        }

        targetPlanMember.setRole(request.getRole());

        planMemberRepository.save(targetPlanMember);

        return planMemberMapper.planMemberToPlanMemberResponseDTO(targetPlanMember);
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
        PlanMember planMember = planMemberRepository.findByPlanIdAndUserIdWithUser(planId, userId);
        if (planMember == null) {
            return false;
        }
        // Check if the user's role meets or exceeds the required role
        return planMember.getRole().ordinal() <= requiredRole.ordinal();
    }
}