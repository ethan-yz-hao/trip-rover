package org.ethanhao.triprover.filter;

import org.ethanhao.triprover.domain.LoginUser;
import org.ethanhao.triprover.domain.PlanMember;
import org.ethanhao.triprover.service.PlanService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.lang.NonNull;

@Component
public class JwtChannelInterceptor implements ChannelInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(JwtChannelInterceptor.class);

    private final PlanService planService;

    @Autowired
    public JwtChannelInterceptor(PlanService planService) {
        this.planService = planService;
    }

    @Override
    public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) {
            logger.error("Failed to get StompHeaderAccessor from message");
            throw new AccessDeniedException("Invalid message format");
        }
        
        if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            String destination = accessor.getDestination();

            if (destination != null && destination.startsWith("/topic/plan/")) {
                // Extract planId from the destination
                String planIdStr = destination.substring("/topic/plan/".length());
                String[] parts = planIdStr.split("/");
                Long planId = Long.valueOf(parts[0]);

                // Get the authenticated user
                Authentication authentication = null;
                if (accessor.getUser() instanceof Authentication) {
                    authentication = (Authentication) accessor.getUser();
                }
                if (authentication == null || !(authentication.getPrincipal() instanceof LoginUser)) {
                    throw new AccessDeniedException("User not authenticated");
                }
                LoginUser loginUser = (LoginUser) authentication.getPrincipal();
                Long userId = loginUser.getUser().getId();

                // Check if the user is authorized for this planId
                if (!planService.hasRole(userId, planId, PlanMember.RoleType.VIEWER)) {
                    // User is not authorized, reject the subscription
                    throw new AccessDeniedException("User not authorized to subscribe to this plan");
                }

                logger.info("User {} subscribed to plan {}", userId, planId);
            }
        }
        return message;
    }
}
