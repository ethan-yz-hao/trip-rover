package org.ethanhao.triprover.controller;

import org.ethanhao.triprover.domain.LoginUser;
import org.ethanhao.triprover.dto.GetPlan;
import org.ethanhao.triprover.domain.ResponseResult;
import org.ethanhao.triprover.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @GetMapping("/plans")
    @PreAuthorize("hasAuthority('user:all')")
    public ResponseResult<Object> getUserPlans(Authentication authentication) {
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        Long userId = loginUser.getUser().getId();
        
        logger.info("Retrieving plans for user: {} (ID: {})", 
            loginUser.getUser().getUserName(), 
            userId);
        
        try {
            List<GetPlan> plans = userService.getUserPlans(userId);
            return new ResponseResult<>(200, "Success", plans);
        } catch (Exception e) {
            logger.error("Failed to retrieve user plans", e);
            return new ResponseResult<>(500, 
                "Failed to retrieve user plans: " + e.getMessage());
        }
    }
    
}