package org.ethanhao.triprover.filter;

import io.jsonwebtoken.ExpiredJwtException;
import org.ethanhao.triprover.domain.LoginUser;
import org.ethanhao.triprover.utils.JwtUtil;
import org.ethanhao.triprover.utils.RedisCache;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Objects;

@Component
public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {
    @Autowired
    RedisCache redisCache;

    @Autowired
    JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // get token from request header
        String token = request.getHeader("Authorization");

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
            throw new RuntimeException("Access token expired");
        } catch (Exception e) {
            throw new RuntimeException("Invalid token");
        }

        // get user info from redis
        String redisKey = "login:" + userId;
        LoginUser loginUser = redisCache.getCacheObject(redisKey);

        // check if loginUser is empty
        if (Objects.isNull(loginUser)) {
            throw new RuntimeException("User not logged in");
        }

        // store user info in SecurityContextHolder (user info, permission info)
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(loginUser, null, loginUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);

        // let it pass through
        filterChain.doFilter(request, response);
    }
}
