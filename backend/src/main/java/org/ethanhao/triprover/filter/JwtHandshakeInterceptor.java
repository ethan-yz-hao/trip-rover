package org.ethanhao.triprover.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.ethanhao.triprover.domain.LoginUser;
import org.ethanhao.triprover.utils.JwtUtil;
import org.ethanhao.triprover.utils.RedisCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.Map;
import java.util.Objects;

@Component
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(JwtHandshakeInterceptor.class);

    private final JwtUtil jwtUtil;
    private final RedisCache redisCache;

    @Autowired
    public JwtHandshakeInterceptor(JwtUtil jwtUtil, RedisCache redisCache) {
        this.jwtUtil = jwtUtil;
        this.redisCache = redisCache;
    }

    @Override
    public boolean beforeHandshake(@NonNull ServerHttpRequest request, @NonNull ServerHttpResponse response,
                                   @NonNull WebSocketHandler wsHandler, @NonNull Map<String, Object> attributes) throws Exception {
        String token = null;
        if (request instanceof ServletServerHttpRequest) {
            HttpServletRequest servletRequest = ((ServletServerHttpRequest) request).getServletRequest();
            Cookie[] cookies = servletRequest.getCookies();

            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("JWT".equals(cookie.getName())) {
                        token = cookie.getValue();
                        break;
                    }
                }
            }
        }

        if (!StringUtils.hasText(token)) {
            logger.warn("WebSocket connection attempt without JWT token");
            return false; // Reject the handshake
        }

        try {
            // Parse JWT token
            Claims claims = jwtUtil.parseJWT(token);
            String userId = claims.getSubject();

            // Retrieve user from Redis
            String redisKey = "login:" + userId;
            LoginUser loginUser = redisCache.getCacheObject(redisKey);

            if (Objects.isNull(loginUser)) {
                logger.warn("WebSocket connection attempt with invalid JWT token");
                return false; // Reject the handshake
            }

            // Create Authentication token
            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(loginUser, null, loginUser.getAuthorities());

            // Set the authentication in SecurityContext
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);

            logger.info("User {} connected to WebSocket", loginUser.getUser().getUserName());

            return true; // Accept the handshake

        } catch (ExpiredJwtException e) {
            logger.error("JWT token expired: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
        }

        return false; // Reject the handshake on failure
    }

    @Override
    public void afterHandshake(@NonNull ServerHttpRequest request, @NonNull ServerHttpResponse response,
                               @NonNull WebSocketHandler wsHandler, @Nullable Exception exception) {
        // No-op
    }
}
