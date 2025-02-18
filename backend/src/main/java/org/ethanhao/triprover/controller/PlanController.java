package org.ethanhao.triprover.controller;

import java.util.List;

import org.ethanhao.triprover.domain.LoginUser;
import org.ethanhao.triprover.domain.PlanMember;
import org.ethanhao.triprover.domain.ResponseResult;
import org.ethanhao.triprover.dto.plan.PlanBaseDTO;
import org.ethanhao.triprover.dto.plan.PlanIndexResponseDTO;
import org.ethanhao.triprover.dto.plan.PlanPlacesResponseDTO;
import org.ethanhao.triprover.dto.plan.PlanSummaryResponseDTO;
import org.ethanhao.triprover.dto.plan.PlanUpdateDTO;
import org.ethanhao.triprover.dto.plan.member.BatchPlanMemberAdditionResponseDTO;
import org.ethanhao.triprover.dto.plan.member.PlanMemberBaseDTO;
import org.ethanhao.triprover.dto.plan.member.PlanMemberResponseDTO;
import org.ethanhao.triprover.dto.plan.member.PlanMemberUpdateDTO;
import org.ethanhao.triprover.service.PlanService;
import org.ethanhao.triprover.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/plan")
@Slf4j
public class PlanController {

    private final PlanService planService;
    private final SearchService searchService;

    @Autowired
    public PlanController(PlanService planService, SearchService searchService) {
        this.planService = planService;
        this.searchService = searchService;
    }

    @GetMapping()
    @PreAuthorize("hasAuthority('user:all')")
    public ResponseResult<List<PlanSummaryResponseDTO>> getPlanSummaries(Authentication authentication) {
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        Long userId = loginUser.getUser().getId();

        log.info("Retrieving plans for user: {} (ID: {})",
                loginUser.getUser().getUserName(),
                userId);

        List<PlanSummaryResponseDTO> planSummaries = planService.getPlanSummaries(userId);
        return new ResponseResult<>(200, "Success", planSummaries);
    }

    @GetMapping("/{planId}")
    @PreAuthorize("hasAuthority('user:all')")
    public ResponseResult<PlanSummaryResponseDTO> getPlanSummary(
            @PathVariable Long planId,
            Authentication authentication) {
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

        log.info("Creating new plan '{}' for user: {} (ID: {})",
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
            Authentication authentication) {
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        Long userId = loginUser.getUser().getId();

        if (!planService.hasRole(userId, planId, PlanMember.RoleType.OWNER)) {
            log.info("User {} is not authorized to delete plan {}", userId, planId);
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
            Authentication authentication) {
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        Long userId = loginUser.getUser().getId();

        if (!planService.hasRole(userId, planId, PlanMember.RoleType.EDITOR)) {
            log.info("User {} is not authorized to update plan {}", userId, planId);
            throw new AccessDeniedException("User is not authorized to update plan");
        }

        PlanSummaryResponseDTO planSummary = planService.updatePlan(userId, planId, request);
        return new ResponseResult<>(200, "Success", planSummary);
    }

    @GetMapping("/{planId}/member")
    @PreAuthorize("hasAuthority('user:all')")
    public ResponseResult<List<PlanMemberResponseDTO>> getPlanMembers(
            @PathVariable Long planId,
            Authentication authentication) {
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        Long userId = loginUser.getUser().getId();

        if (!planService.hasRole(userId, planId, PlanMember.RoleType.VIEWER)) {
            log.info("User {} is not authorized to view members of plan {}", userId, planId);
            throw new AccessDeniedException("User is not authorized to view plan members");
        }

        List<PlanMemberResponseDTO> planMembers = planService.getPlanMembers(planId);
        return new ResponseResult<>(200, "Success", planMembers);
    }

    @PostMapping("/{planId}/member")
    @PreAuthorize("hasAuthority('user:all')")
    public ResponseResult<BatchPlanMemberAdditionResponseDTO> addPlanMembers(
            @PathVariable Long planId,
            @Valid @RequestBody List<PlanMemberUpdateDTO> requests,
            Authentication authentication) {
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        Long userId = loginUser.getUser().getId();

        if (requests.isEmpty()) {
            return new ResponseResult<>(400, "Request list cannot be empty", null);
        }

        if (!planService.hasRole(userId, planId, PlanMember.RoleType.EDITOR)) {
            log.info("User {} is not authorized to add members to plan {}", userId, planId);
            throw new AccessDeniedException("User is not authorized to add members to plan");
        }

        BatchPlanMemberAdditionResponseDTO result = planService.addPlanMembers(userId, planId, requests);
        return new ResponseResult<>(200, "Members processed", result);
    }

    @DeleteMapping("/{planId}/member")
    @PreAuthorize("hasAuthority('user:all')")
    public ResponseResult<Void> removePlanMember(
            @PathVariable Long planId,
            @Valid @RequestBody PlanMemberBaseDTO request,
            Authentication authentication) {
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        Long userId = loginUser.getUser().getId();

        if (!planService.hasRole(userId, planId, PlanMember.RoleType.EDITOR)) {
            log.info("User {} is not authorized to remove member from plan {}", userId, planId);
            throw new AccessDeniedException("User is not authorized to remove member from plan");
        }

        planService.removePlanMember(userId, planId, request);
        return new ResponseResult<>(200, "Success");
    }

    @PatchMapping("/{planId}/member/role")
    @PreAuthorize("hasAuthority('user:all')")
    public ResponseResult<PlanMemberResponseDTO> updatePlanMemberRole(
            @PathVariable Long planId,
            @Valid @RequestBody PlanMemberUpdateDTO request,
            Authentication authentication) {
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        Long userId = loginUser.getUser().getId();

        if (!planService.hasRole(userId, planId, PlanMember.RoleType.EDITOR)) {
            log.info("User {} is not authorized to update member role in plan {}", userId, planId);
            throw new AccessDeniedException("User is not authorized to update member role in plan");
        }

        PlanMemberResponseDTO planMember = planService.updatePlanMemberRole(userId, planId, request);
        return new ResponseResult<>(200, "Success", planMember);
    }

    @GetMapping("/{planId}/places")
    @PreAuthorize("hasAuthority('user:all')")
    public ResponseResult<PlanPlacesResponseDTO> getPlanPlace(
            @PathVariable Long planId,
            Authentication authentication) {
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        Long userId = loginUser.getUser().getId();

        log.info("User {} fetching plan {}", userId, planId);
        if (!planService.hasRole(userId, planId, PlanMember.RoleType.VIEWER)) {
            log.info("User {} is not authorized to fetch plan {}", userId, planId);
            throw new AccessDeniedException("User is not authorized to fetch plan");
        }

        PlanPlacesResponseDTO planPlaces = planService.getPlanPlaces(planId);
        return new ResponseResult<>(200, "Success", planPlaces);
    }

    @GetMapping("/search")
    @PreAuthorize("hasAuthority('user:all')")
    public ResponseResult<List<PlanIndexResponseDTO>> searchPlans(
            @RequestParam String query) {
        List<PlanIndexResponseDTO> plans = searchService.searchPlans(query);
        return new ResponseResult<>(
                HttpStatus.OK.value(),
                "Search successful",
                plans);
    }
}