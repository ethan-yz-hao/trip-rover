package org.ethanhao.triprover.service.impl;

import java.time.Duration;
import java.util.Arrays;
import java.util.Objects;

import org.ethanhao.triprover.domain.LoginUser;
import org.ethanhao.triprover.domain.User;
import org.ethanhao.triprover.dto.user.UserAuthDTO;
import org.ethanhao.triprover.dto.user.UserRegisterDTO;
import org.ethanhao.triprover.dto.user.UserResponseDTO;
import org.ethanhao.triprover.dto.user.UserUpdateDTO;
import org.ethanhao.triprover.mapper.UserMapper;
import org.ethanhao.triprover.repository.UserRepository;
import org.ethanhao.triprover.service.DBUserDetailsManager;
import org.ethanhao.triprover.service.UserService;
import org.ethanhao.triprover.utils.JwtUtil;
import org.ethanhao.triprover.utils.RedisCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class UserServiceImpl implements UserService {
    
    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    DBUserDetailsManager dbUserDetailsManager;

    @Autowired
    UserRepository userRepository;
    
    @Autowired
    RedisCache redisCache;

    @Autowired
    JwtUtil jwtUtil;

    @Value("${JWT_TTL}")
    private Long jwtTtl;

    @Autowired
    private UserMapper userMapper;

    @Override
    public void login(UserAuthDTO loginRequest, HttpServletResponse response) {
        User user = userMapper.userAuthDtoToUser(loginRequest);
        // Encapsulate the Authentication object
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(user.getUserName(), user.getPassword());
        // Authenticate the user through the authenticate method of AuthenticationManager
        Authentication authenticated = authenticationManager.authenticate(authenticationToken);
        // If the authentication fails, throw an exception
        if (Objects.isNull(authenticated)) {
            throw new AuthenticationCredentialsNotFoundException("Login failed");
        }
        // Get user information in Authentication
        LoginUser loginUser = (LoginUser) authenticated.getPrincipal();
        String userId = loginUser.getUser().getId().toString();
        // Generate token after authentication
        String jwt = jwtUtil.createJWT(userId, jwtTtl);
        // Store user information in redis
        redisCache.setCacheObject("login:" + userId, loginUser);
        // Set JWT as an HTTP-only, Secure cookie
        ResponseCookie cookie = ResponseCookie.from("JWT", jwt)
                .httpOnly(true)
                .secure(false) // Need to use HTTPS in production
                .path("/")
                .maxAge(Duration.ofMillis(jwtTtl)) // The cookie will expire after the JWT token expires
                .sameSite("Lax") // Adjust as needed (Strict, Lax, None)
                .build();

        // Set the cookie in the response header
        response.setHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        log.info("User {} logged in", loginUser.getUser().getUserName());
    }

    @Override
    public void logout(HttpServletResponse response) {
        // Get the user id from SecurityContextHolder
        UsernamePasswordAuthenticationToken authentication = (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        String userId = loginUser.getUser().getId().toString();
        // Delete user information from redis
        redisCache.deleteObject("login:" + userId);
        // Clear the Cookie
        ResponseCookie cookie = ResponseCookie.from("JWT", "")
                .httpOnly(true)
                .secure(false) // Need to use HTTPS in production
                .path("/")
                .maxAge(Duration.ZERO)
                .sameSite("Lax") // Adjust as needed (Strict, Lax, None)
                .build();
        response.setHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        log.info("User {} logged out", loginUser.getUser().getUserName());
    }

    @Override
    public UserResponseDTO register(UserRegisterDTO registerRequest) {
        User user = userMapper.userRegisterDtoToUser(registerRequest);
        dbUserDetailsManager.createUserWithRole(user, Arrays.asList("user"));
        return userMapper.toResponseDto(user);
    }

    @Override
    public UserResponseDTO updateUser(Long userId, UserUpdateDTO updateRequest) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        userMapper.updateUserFromDto(updateRequest, user);
        User updatedUser = userRepository.save(user);
        return userMapper.toResponseDto(updatedUser);
    }

    @Override
    public void deleteUser(String username) {
        dbUserDetailsManager.deleteUser(username);
    }

    @Override
    public void changePassword(String oldPassword, String newPassword) {
        dbUserDetailsManager.changePassword(oldPassword, newPassword);
    }
}
