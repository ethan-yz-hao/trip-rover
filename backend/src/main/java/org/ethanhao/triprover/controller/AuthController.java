package org.ethanhao.triprover.controller;

import org.ethanhao.triprover.domain.LoginUser;
import org.ethanhao.triprover.domain.ResponseResult;
import org.ethanhao.triprover.domain.User;
import org.ethanhao.triprover.dto.user.UserAuthDTO;
import org.ethanhao.triprover.dto.user.UserRegisterDTO;
import org.ethanhao.triprover.dto.user.UserResponseDTO;
import org.ethanhao.triprover.dto.user.UserUpdateDTO;
import org.ethanhao.triprover.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/user")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ResponseResult<Object> login(@Valid @RequestBody UserAuthDTO loginRequest, HttpServletResponse response) {
        return authService.login(loginRequest, response);
    }

    @PostMapping("/logout")
    public ResponseResult<Object> logout(HttpServletResponse response) {
        return authService.logout(response);
    }

    @PostMapping("/register")
    public ResponseResult<UserResponseDTO> register(@Valid @RequestBody UserRegisterDTO registerRequest) {
        return authService.register(registerRequest);
    }

    @PostMapping("/update")
    @PreAuthorize("hasAuthority('system:user:update')")
    public ResponseResult<UserResponseDTO> updatePassword(@RequestBody UserUpdateDTO updateRequest, Authentication authentication) {
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        Long userId = loginUser.getUser().getId();
        return authService.updateUser(userId, updateRequest);
    }

    @PostMapping("/user/delete")
    @PreAuthorize("hasAuthority('system:user:delete')")
    public ResponseResult<Object> deleteUser(@RequestBody User user) {
        return authService.deleteUser(user);
    }
}
