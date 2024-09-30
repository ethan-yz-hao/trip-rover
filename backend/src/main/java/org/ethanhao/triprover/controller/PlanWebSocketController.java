package org.ethanhao.triprover.controller;

import org.ethanhao.triprover.domain.LoginUser;
import org.ethanhao.triprover.domain.PlanUpdateMessage;
import org.ethanhao.triprover.service.PlanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;


@Controller
public class PlanWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final PlanService planService;

    @Autowired
    public PlanWebSocketController(SimpMessagingTemplate messagingTemplate, PlanService planService) {
        this.messagingTemplate = messagingTemplate;
        this.planService = planService;
    }

    @MessageMapping("/plan/{planId}/update")
    public void updatePlan(
            @DestinationVariable Long planId,
            @Payload PlanUpdateMessage updateMessage,
            Authentication authentication
    ) {
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        Long userId = loginUser.getUser().getId();

        // Check if the user is authorized
        if (!planService.isUserAuthorized(planId, userId)) {
            // Send an error message to the user
            messagingTemplate.convertAndSendToUser(
                    userId.toString(), "/queue/errors", "Unauthorized access to plan."
            );
            return;
        }

        try {
            // Apply the update to the plan
            planService.applyUpdate(planId, updateMessage);

            // Broadcast the update to all subscribers of the plan's topic
            messagingTemplate.convertAndSend("/topic/plan/" + planId, updateMessage);

        } catch (Exception e) {
            // Handle exceptions and send error messages if necessary
            messagingTemplate.convertAndSendToUser(
                    userId.toString(), "/queue/errors", "An error occurred while updating the plan."
            );
            e.printStackTrace();
        }
    }
}
