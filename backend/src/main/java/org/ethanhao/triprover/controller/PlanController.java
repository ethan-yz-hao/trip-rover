package org.ethanhao.triprover.controller;

import java.util.List;

import org.ethanhao.triprover.domain.LoginUser;
import org.ethanhao.triprover.domain.PlanMember;
import org.ethanhao.triprover.domain.ResponseResult;
import org.ethanhao.triprover.dto.plan.PlanBaseDTO;
import org.ethanhao.triprover.dto.plan.PlanPlacesResponseDTO;
import org.ethanhao.triprover.dto.plan.PlanSummaryResponseDTO;
import org.ethanhao.triprover.dto.plan.PlanUpdateDTO;
import org.ethanhao.triprover.dto.plan.member.PlanMemberBaseDTO;
import org.ethanhao.triprover.dto.plan.member.PlanMemberUpdateDTO;
import org.ethanhao.triprover.service.PlanService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;


@RestController
@RequestMapping("/api/plan")
public class PlanController {

    private final PlanService planService;
    private static final Logger logger = LoggerFactory.getLogger(PlanController.class);

    @Autowired
    public PlanController(PlanService planService) {
        this.planService = planService;
    }

    @GetMapping()
    @PreAuthorize("hasAuthority('user:all')")
    public ResponseResult<List<PlanSummaryResponseDTO>> getPlanSummaries(Authentication authentication) {
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        Long userId = loginUser.getUser().getId();
        
        logger.info("Retrieving plans for user: {} (ID: {})", 
            loginUser.getUser().getUserName(), 
            userId);
        
        List<PlanSummaryResponseDTO> planSummaries = planService.getPlanSummaries(userId);
        return new ResponseResult<>(200, "Success", planSummaries);
    }

    @GetMapping("/{planId}")
    @PreAuthorize("hasAuthority('user:all')")
    public ResponseResult<PlanSummaryResponseDTO> getPlanSummary(
            @PathVariable Long planId,
            Authentication authentication
    ) {
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        Long userId = loginUser.getUser().getId();

        PlanSummaryResponseDTO planSummary = planService.getPlanSummary(userId, planId);
        return new ResponseResult<>(200, "Success", planSummary);
    }
    
    @PostMapping()
    @PreAuthorize("hasAuthority('user:all')")
    public ResponseResult<PlanSummaryResponseDTO> createPlan(
            @Valid @RequestBody PlanBaseDTO request,
            Authentication authentication) {
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        Long userId = loginUser.getUser().getId();
        
        logger.info("Creating new plan '{}' for user: {} (ID: {})", 
            request.getPlanName(), 
            loginUser.getUser().getUserName(), 
            userId);
        
        PlanSummaryResponseDTO planSummary = planService.createPlan(userId, request);
        return new ResponseResult<>(200, "Success", planSummary);
    }

    @DeleteMapping("/{planId}")
    @PreAuthorize("hasAuthority('user:all')")
    public ResponseResult<Void> deletePlan(
            @PathVariable Long planId,
            Authentication authentication
    ) {
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        Long userId = loginUser.getUser().getId();

        if (!planService.hasRole(userId, planId, PlanMember.RoleType.OWNER)) {  
            logger.info("User {} is not authorized to delete plan {}", userId, planId);
            throw new AccessDeniedException("User is not authorized to delete plan");
        }

        planService.deletePlan(planId);
        return new ResponseResult<>(200, "Success", null);
    }

    @PatchMapping("/{planId}")
    @PreAuthorize("hasAuthority('user:all')")
    public ResponseResult<PlanSummaryResponseDTO> updatePlan(
            @PathVariable Long planId,
            @Valid @RequestBody PlanUpdateDTO request,
            Authentication authentication
    ) {
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        Long userId = loginUser.getUser().getId();

        if (!planService.hasRole(userId, planId, PlanMember.RoleType.EDITOR)) {
            logger.info("User {} is not authorized to update plan {}", userId, planId);
            throw new AccessDeniedException("User is not authorized to delete plan");
        }

        PlanSummaryResponseDTO planSummary = planService.updatePlan(userId, planId, request);
        return new ResponseResult<>(200, "Success", planSummary);
    }

    @PostMapping("/member")
    @PreAuthorize("hasAuthority('user:all')")
    public ResponseResult<PlanSummaryResponseDTO> addPlanMember(
            @Valid @RequestBody PlanMemberUpdateDTO request,
            Authentication authentication
    ) {
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        Long userId = loginUser.getUser().getId();
        Long planId = request.getPlanId();

        if (!planService.hasRole(userId, planId, PlanMember.RoleType.EDITOR)) {
            logger.info("User {} is not authorized to add member to plan {}", userId, planId);
            throw new AccessDeniedException("User is not authorized to add member to plan");
        }

        PlanSummaryResponseDTO planSummary = planService.addPlanMember(userId, request);
        return new ResponseResult<>(200, "Success", planSummary);
    }

    @DeleteMapping("/member")
    @PreAuthorize("hasAuthority('user:all')")
    public ResponseResult<PlanSummaryResponseDTO> removePlanMember(
            @Valid @RequestBody PlanMemberBaseDTO request,
            Authentication authentication
    ) {
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        Long userId = loginUser.getUser().getId();
        Long planId = request.getPlanId();

        if (!planService.hasRole(userId, planId, PlanMember.RoleType.EDITOR)) {
            logger.info("User {} is not authorized to remove member from plan {}", userId, planId);
            throw new AccessDeniedException("User is not authorized to remove member from plan");
        }

        PlanSummaryResponseDTO planSummary = planService.removePlanMember(userId, request);
        return new ResponseResult<>(200, "Success", planSummary);
    }

    @PatchMapping("/member/role")
    @PreAuthorize("hasAuthority('user:all')")
    public ResponseResult<PlanSummaryResponseDTO> updatePlanMemberRole(
            @Valid @RequestBody PlanMemberUpdateDTO request,
            Authentication authentication
    ) {
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        Long userId = loginUser.getUser().getId();
        Long planId = request.getPlanId();

        if (!planService.hasRole(userId, planId, PlanMember.RoleType.EDITOR)) {
            logger.info("User {} is not authorized to update member role in plan {}", userId, planId);
            throw new AccessDeniedException("User is not authorized to update member role in plan");
        }

        PlanSummaryResponseDTO planSummary = planService.updatePlanMemberRole(userId, request);
        return new ResponseResult<>(200, "Success", planSummary);
    }

    @GetMapping("/{planId}/places")
    @PreAuthorize("hasAuthority('user:all')")
    public ResponseResult<PlanPlacesResponseDTO> getPlanPlace(
            @PathVariable Long planId,
            Authentication authentication
    ) {
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        Long userId = loginUser.getUser().getId();

        logger.info("User {} fetching plan {}", userId, planId);
        if (!planService.hasRole(userId, planId, PlanMember.RoleType.VIEWER)) {
            logger.info("User {} is not authorized to fetch plan {}", userId, planId);
            throw new AccessDeniedException("User is not authorized to fetch plan");
        }

        PlanPlacesResponseDTO planPlaces = planService.getPlanPlaces(planId);
        return new ResponseResult<>(200, "Success", planPlaces);
    }

}