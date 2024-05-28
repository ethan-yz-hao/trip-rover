package org.ethanhao.triprover.service.impl;

import org.ethanhao.triprover.domain.LoginUser;
import org.ethanhao.triprover.domain.ResponseResult;
import org.ethanhao.triprover.domain.User;
import org.ethanhao.triprover.service.DBUserDetailsManager;
import org.ethanhao.triprover.service.LoginService;
import org.ethanhao.triprover.utils.JwtUtil;
import org.ethanhao.triprover.utils.RedisCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Service
public class LoginServiceImpl implements LoginService {
    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    DBUserDetailsManager dbUserDetailsManager;

    @Autowired
    RedisCache redisCache;

    @Override
    public ResponseResult login(User user) {
        // Encapsulate the Authentication object
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(user.getUserName(), user.getPassword());
        // Authenticate the user through the authenticate method of AuthenticationManager
        Authentication authenticated = authenticationManager.authenticate(authenticationToken);
        // If the authentication fails, throw an exception
        if (Objects.isNull(authenticated)) {
            throw new RuntimeException("Login failed");
        }
        // Get user information in Authentication
        LoginUser loginUser = (LoginUser) authenticated.getPrincipal();
        String userId = loginUser.getUser().getId().toString();
        // Generate token after authentication
        String jwt = JwtUtil.createJWT(userId);
        // Store user information in redis
        redisCache.setCacheObject("login:" + userId, loginUser);
        // Return the token to the front end
        Map<String, String> hashMap = new HashMap<>();
        hashMap.put("token", jwt);
        return new ResponseResult(HttpStatus.OK.value(), "Login successful", hashMap);
    }

    @Override
    public ResponseResult logout() {
        // Get the user id from SecurityContextHolder
        UsernamePasswordAuthenticationToken authentication = (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        Long userId = loginUser.getUser().getId();
        // Delete user information from redis
        redisCache.deleteObject("login:" + userId);
        return new ResponseResult(HttpStatus.OK.value(), "Logout successful");
    }

    @Override
    public ResponseResult register(User user) {
        try {
            dbUserDetailsManager.createUserWithRole(user, Arrays.asList("user"));
        } catch  (Exception e) {
            return new ResponseResult(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to register user: " + e.getMessage());
        }
        return new ResponseResult(HttpStatus.OK.value(), "Register successful");
    }
}
