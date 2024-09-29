package org.ethanhao.triprover.controller;

import org.ethanhao.triprover.domain.LoginUser;
import org.ethanhao.triprover.domain.Plan;
import org.ethanhao.triprover.domain.PlanPlace;
import org.ethanhao.triprover.service.PlanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/plan")
public class PlanController {

    private final PlanService planService;

    @Autowired
    public PlanController(PlanService planService) {
        this.planService = planService;
    }

    @GetMapping("/{planId}")
    @PreAuthorize("hasAuthority('user:all')")
    public ResponseEntity<Plan> getPlan(
            @PathVariable Long planId,
            Authentication authentication
    ) {
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        Long userId = loginUser.getUser().getId();
        if (!planService.isUserAuthorized(planId, userId)) {
            return ResponseEntity.status(403).build(); // Forbidden
        }

        Plan plan = planService.getPlan(planId);
        return ResponseEntity.ok(plan);
    }

    @GetMapping("/{planId}/place")
    @PreAuthorize("hasAuthority('user:all')")
    public ResponseEntity<List<PlanPlace>> getPlanPlace(
            @PathVariable Long planId,
            Authentication authentication
    ) {
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        Long userId = loginUser.getUser().getId();
        if (!planService.isUserAuthorized(planId, userId)) {
            return ResponseEntity.status(403).build(); // Forbidden
        }

        List<PlanPlace> places = planService.getPlanPlace(planId);
        return ResponseEntity.ok(places);
    }
}