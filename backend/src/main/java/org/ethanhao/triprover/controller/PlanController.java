package org.ethanhao.triprover.controller;

import java.util.List;

import org.ethanhao.triprover.domain.LoginUser;
import org.ethanhao.triprover.domain.PlanMember;
import org.ethanhao.triprover.domain.ResponseResult;
import org.ethanhao.triprover.dto.PlanCreation;
import org.ethanhao.triprover.dto.PlanPlaces;
import org.ethanhao.triprover.dto.PlanSummary;
import org.ethanhao.triprover.service.PlanService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
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
    public ResponseResult<List<PlanSummary>> getPlanSummaries(Authentication authentication) {
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        Long userId = loginUser.getUser().getId();
        
        logger.info("Retrieving plans for user: {} (ID: {})", 
            loginUser.getUser().getUserName(), 
            userId);
        
        try {
            List<PlanSummary> planSummaries = planService.getPlanSummaries(userId);
            return new ResponseResult<>(200, "Success", planSummaries);
        } catch (Exception e) {
            logger.error("Failed to retrieve user plan summaries", e);
            return new ResponseResult<>(500, 
                "Failed to retrieve user plan summaries: " + e.getMessage());
        }
    }

    @GetMapping("/{planId}")
    @PreAuthorize("hasAuthority('user:all')")
    public ResponseResult<PlanSummary> getPlanSummary(
            @PathVariable Long planId,
            Authentication authentication
    ) {
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        Long userId = loginUser.getUser().getId();

        try  {
            PlanSummary planSummary = planService.getPlanSummary(userId, planId);
            return new ResponseResult<>(200, "Success", planSummary);
        } catch (Exception e) {
            logger.error("Failed to retrieve plan summary for user {} and plan {}", userId, planId, e);
            return new ResponseResult<>(500, 
                "Failed to retrieve plan summary: " + e.getMessage());
        }
    }
    
    @PostMapping()
    @PreAuthorize("hasAuthority('user:all')")
    public ResponseResult<PlanSummary> createPlan(
            @Valid @RequestBody PlanCreation request,
            Authentication authentication) {
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        Long userId = loginUser.getUser().getId();
        
        logger.info("Creating new plan '{}' for user: {} (ID: {})", 
            request.getPlanName(), 
            loginUser.getUser().getUserName(), 
            userId);
        
        try {
            PlanSummary planSummary = planService.createPlan(userId, request);
            return new ResponseResult<>(200, "Success", planSummary);
        } catch (Exception e) {
            logger.error("Failed to create plan", e);
            return new ResponseResult<>(500, 
                "Failed to create plan: " + e.getMessage());
        }
    }

    @GetMapping("/{planId}/places")
    @PreAuthorize("hasAuthority('user:all')")
    public ResponseEntity<PlanPlaces> getPlanPlace(
            @PathVariable Long planId,
            Authentication authentication
    ) {
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        Long userId = loginUser.getUser().getId();

        logger.info("User {} fetching plan {}", userId, planId);
        if (!planService.hasRole(planId, userId, PlanMember.RoleType.VIEWER)) {
            logger.info("User {} is not authorized to fetch plan {}", userId, planId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build(); // Forbidden
        }

        PlanPlaces planPlaces = planService.getPlanPlaces(planId);
        return ResponseEntity.ok(planPlaces);
    }

}