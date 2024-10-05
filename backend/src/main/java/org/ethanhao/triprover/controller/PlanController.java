package org.ethanhao.triprover.controller;

import org.ethanhao.triprover.domain.LoginUser;
import org.ethanhao.triprover.domain.Plan;
import org.ethanhao.triprover.service.PlanService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/plan")
public class PlanController {

    private final PlanService planService;
    private static final Logger logger = LoggerFactory.getLogger(PlanController.class);

    @Autowired
    public PlanController(PlanService planService) {
        this.planService = planService;
    }

    @GetMapping("/{planId}")
    @PreAuthorize("hasAuthority('user:all')")
    public ResponseEntity<Plan> getPlanPlace(
            @PathVariable Long planId,
            Authentication authentication
    ) {
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        Long userId = loginUser.getUser().getId();

        logger.info("User {} fetching plan {}", userId, planId);
        if (!planService.isUserAuthorized(planId, userId)) {
            logger.info("User {} is not authorized to fetch plan {}", userId, planId);
            return ResponseEntity.status(403).build(); // Forbidden
        }

        Plan plan = planService.getPlan(planId);
        return ResponseEntity.ok(plan);
    }
}