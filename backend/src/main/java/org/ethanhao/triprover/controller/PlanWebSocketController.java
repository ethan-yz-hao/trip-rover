package org.ethanhao.triprover.controller;

import org.ethanhao.triprover.domain.LoginUser;
import org.ethanhao.triprover.domain.PlanAckMessage;
import org.ethanhao.triprover.domain.PlanMember;
import org.ethanhao.triprover.domain.PlanUpdateMessage;
import org.ethanhao.triprover.exception.PlanOperationException;
import org.ethanhao.triprover.service.PlanService;
import org.ethanhao.triprover.service.PlanUpdateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import jakarta.persistence.OptimisticLockException;
import lombok.extern.slf4j.Slf4j;


@Controller
@Slf4j
public class PlanWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final PlanService planService;
    private final PlanUpdateService planUpdateService;

    @Autowired
    public PlanWebSocketController(SimpMessagingTemplate messagingTemplate, PlanService planService, PlanUpdateService planUpdateService) {
        this.messagingTemplate = messagingTemplate;
        this.planService = planService;
        this.planUpdateService = planUpdateService;
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
        if (!planService.hasRole(userId, planId, PlanMember.RoleType.EDITOR)) {
            // Send an error message to the user
            log.info("User {} is not authorized to update plan {}", userId, planId);
            PlanAckMessage ackMessage = new PlanAckMessage(updateMessage.getUpdateId(), PlanAckMessage.StatusType.ERROR, "User not authorized");
            messagingTemplate.convertAndSend("/topic/plan/" + planId + "/ack/" + updateMessage.getClientId(), ackMessage);
            return;
        }

        try {
            log.info("User {} updating plan {} with message: {}", userId, planId, updateMessage);
            // Apply the update to the plan
            Long newVersion = planUpdateService.updatePlanWithMessage(planId, updateMessage);

            // Set the new version in the update message
            updateMessage.setVersion(newVersion);

            // Broadcast the update to all subscribers of the plan's topic with the new version
            messagingTemplate.convertAndSend("/topic/plan/" + planId, updateMessage);

            // Send an acknowledgment message to the user
            PlanAckMessage ackMessage = new PlanAckMessage(updateMessage.getUpdateId(), PlanAckMessage.StatusType.OK, null);
            messagingTemplate.convertAndSend("/topic/plan/" + planId + "/ack/" + updateMessage.getClientId(), ackMessage);
        } catch (OptimisticLockException e) {
            // Handle optimistic lock exceptions
            log.error("Optimistic lock exception when updating plan {} by user {}: {}", planId, userId, e.getMessage());

            // Send an error message to the user
            PlanAckMessage ackMessage = new PlanAckMessage(updateMessage.getUpdateId(), PlanAckMessage.StatusType.ERROR, "Version mismatch");
            messagingTemplate.convertAndSend("/topic/plan/" + planId + "/ack/" + updateMessage.getClientId(), ackMessage);
        } catch (PlanOperationException e) {
            // Handle illegal argument exceptions
            log.error("Illegal argument exception when updating plan {} by user {}: {}", planId, userId, e.getMessage());

            // Send an error message to the user
            PlanAckMessage ackMessage = new PlanAckMessage(updateMessage.getUpdateId(), PlanAckMessage.StatusType.ERROR, e.getMessage());
            messagingTemplate.convertAndSend("/topic/plan/" + planId + "/ack/" + updateMessage.getClientId(), ackMessage);
        } catch (Exception e) {
            // Handle other exceptions
            log.error("Exception when updating plan {} by user {}: {}", planId, userId, e.getMessage());

            // Send an error message to the user
            PlanAckMessage ackMessage = new PlanAckMessage(updateMessage.getUpdateId(), PlanAckMessage.StatusType.ERROR, "Internal server error");
            messagingTemplate.convertAndSend("/topic/plan/" + planId + "/ack/" + updateMessage.getClientId(), ackMessage);
        }
    }
}
