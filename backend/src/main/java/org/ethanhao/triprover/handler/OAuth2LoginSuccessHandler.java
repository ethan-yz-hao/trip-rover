package org.ethanhao.triprover.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.ethanhao.triprover.domain.LoginUser;
import org.ethanhao.triprover.domain.ResponseResult;
import org.ethanhao.triprover.utils.JwtUtil;
import org.ethanhao.triprover.utils.RedisCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Service
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RedisCache redisCache;

    @Value("${JWT_TTL}")
    private Long jwtTtl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();

        String userId = loginUser.getUser().getId().toString();
        // Create a JWT token and store user session in Redis
        String jwtToken = jwtUtil.createJWT(userId, jwtTtl);
        redisCache.setCacheObject("login:" + userId, loginUser);

        // Set JWT as an HTTP-only, Secure cookie
        ResponseCookie cookie = ResponseCookie.from("JWT", jwtToken)
                .httpOnly(true)
                .secure(false) // Need to use HTTPS in production
                .path("/")
                .maxAge(Duration.ofHours(1)) // 1 hour expiration time
                .sameSite("Lax") // Adjust as needed (Strict, Lax, None)
                .build();
        response.setHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        clearAuthenticationAttributes(request);

        response.sendRedirect("/hello");

        // authenticate the user
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
