package org.ethanhao.triprover.filter;

import java.util.Map;
import java.util.Objects;

import org.ethanhao.triprover.domain.LoginUser;
import org.ethanhao.triprover.utils.JwtUtil;
import org.ethanhao.triprover.utils.RedisCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

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
            throw new AuthenticationCredentialsNotFoundException("Missing JWT token");
        }

        // Parse JWT token
        String userId;
        try {
            // parse token
            Claims claims = jwtUtil.parseJWT(token);
            userId = claims.getSubject();
        } catch (ExpiredJwtException e) {
            throw new CredentialsExpiredException("Access token expired");
        } catch (Exception e) {
            throw new BadCredentialsException("Invalid token");
        }

        // Retrieve user from Redis
        String redisKey = "login:" + userId;
        LoginUser loginUser = redisCache.getCacheObject(redisKey);

        if (Objects.isNull(loginUser)) {
            throw new AuthenticationCredentialsNotFoundException("User not logged in");
        }

        // Create Authentication token
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(loginUser, null, loginUser.getAuthorities());

        // Set the authentication in SecurityContext
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);

        log.info("User {} connected to WebSocket", loginUser.getUser().getUserName());

        return true; // Accept the handshake
    }

    @Override
    public void afterHandshake(@NonNull ServerHttpRequest request, @NonNull ServerHttpResponse response,
                               @NonNull WebSocketHandler wsHandler, @Nullable Exception exception) {
        // No-op
    }
}
