package org.ethanhao.triprover.controller;

import org.ethanhao.triprover.domain.LoginUser;
import org.ethanhao.triprover.domain.ResponseResult;
import org.ethanhao.triprover.domain.User;
import org.ethanhao.triprover.dto.user.UserAuthDTO;
import org.ethanhao.triprover.dto.user.UserRegisterDTO;
import org.ethanhao.triprover.dto.user.UserResponseDTO;
import org.ethanhao.triprover.dto.user.UserUpdateDTO;
import org.ethanhao.triprover.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
public class UserController {

    @Autowired
    private UserService authService;

    @PostMapping("/login")
    public ResponseResult<Object> login(@Valid @RequestBody UserAuthDTO loginRequest, HttpServletResponse response) {
        authService.login(loginRequest, response);
        return new ResponseResult<>(HttpStatus.OK.value(), "Login successful");
    }

    @PostMapping("/logout")
    public ResponseResult<Object> logout(HttpServletResponse response) {
        authService.logout(response);
        return new ResponseResult<>(HttpStatus.OK.value(), "Logout successful");
    }

    @PostMapping("/register")
    public ResponseResult<UserResponseDTO> register(@Valid @RequestBody UserRegisterDTO registerRequest) {
        UserResponseDTO userResponseDTO = authService.register(registerRequest);
        return new ResponseResult<>(HttpStatus.OK.value(), "Register successful", userResponseDTO);
    }

    @PostMapping("/update")
    @PreAuthorize("hasAuthority('system:user:update')")
    public ResponseResult<UserResponseDTO> updatePassword(@RequestBody UserUpdateDTO updateRequest, Authentication authentication) {
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        Long userId = loginUser.getUser().getId();
        UserResponseDTO userResponseDTO = authService.updateUser(userId, updateRequest);
        return new ResponseResult<>(HttpStatus.OK.value(), "User update successful", userResponseDTO);
    }

    @PostMapping("/user/delete")
    @PreAuthorize("hasAuthority('system:user:delete')")
    public ResponseResult<Object> deleteUser(@RequestBody User user) {
        authService.deleteUser(user);
        return new ResponseResult<>(HttpStatus.OK.value(), "User delete successful");
    }
}
