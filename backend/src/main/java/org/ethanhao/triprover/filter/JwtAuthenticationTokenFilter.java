package org.ethanhao.triprover.filter;

import java.io.IOException;
import java.util.Objects;

import org.ethanhao.triprover.domain.LoginUser;
import org.ethanhao.triprover.utils.JwtUtil;
import org.ethanhao.triprover.utils.RedisCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationTokenFilter.class);

    @Autowired
    RedisCache redisCache;

    @Autowired
    JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response, 
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        // get token from cookie
        String token = null;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("JWT".equals(cookie.getName())) {
                    token = cookie.getValue();
                    break;
                }
            }
        }

        // check if token is empty
        if (!StringUtils.hasText(token)) {
            // if token is empty, let it pass through, as SecurityContextHolder has no user info at this time, it will be intercepted by the subsequent filter
            filterChain.doFilter(request, response);
            return;
        }

        // parse token to get user id
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

        // get user info from redis
        String redisKey = "login:" + userId;
        LoginUser loginUser = redisCache.getCacheObject(redisKey);

        // check if loginUser is empty
        if (Objects.isNull(loginUser)) {
            throw new AuthenticationCredentialsNotFoundException("User not logged in");
        }

        // store user info in SecurityContextHolder (user info, permission info)
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(loginUser, null, loginUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);

        // let it pass through
        filterChain.doFilter(request, response);

        logger.info("User {} accessed the resource", loginUser.getUser().getUserName());
    }
}
