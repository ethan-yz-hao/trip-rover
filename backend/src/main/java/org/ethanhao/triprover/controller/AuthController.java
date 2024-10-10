package org.ethanhao.triprover.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.ethanhao.triprover.domain.ResponseResult;
import org.ethanhao.triprover.domain.User;
import org.ethanhao.triprover.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ResponseResult login(@RequestBody User user, HttpServletResponse response) {
        return authService.login(user, response);
    }

    @PostMapping("/logout")
    public ResponseResult logout(HttpServletResponse response) {
        return authService.logout(response);
    }

    @PostMapping("/register")
    public ResponseResult register(@RequestBody User user) {
        return authService.register(user);
    }

    @PostMapping("/update")
    @PreAuthorize("hasAuthority('system:user:update')")
    public ResponseResult updatePassword(@RequestBody User user) {
        return authService.updateUser(user);
    }

    @PostMapping("/user/delete")
    @PreAuthorize("hasAuthority('system:user:delete')")
    public ResponseResult deleteUser(@RequestBody User user) {
        return authService.deleteUser(user);
    }
}
