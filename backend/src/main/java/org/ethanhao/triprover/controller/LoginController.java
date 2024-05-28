package org.ethanhao.triprover.controller;

import org.ethanhao.triprover.domain.ResponseResult;
import org.ethanhao.triprover.domain.User;
import org.ethanhao.triprover.service.LoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LoginController {

    @Autowired
    private LoginService loginService;

    @PostMapping("/user/login")
    public ResponseResult login(@RequestBody User user) {
        return loginService.login(user);
    }

    @PostMapping("/user/logout")
    public ResponseResult logout() {
        return loginService.logout();
    }

    @PostMapping("/user/register")
    public ResponseResult register(@RequestBody User user) {
        return loginService.register(user);
    }

    @PostMapping("/user/update")
    @PreAuthorize("hasAuthority('system:user:update')")
    public ResponseResult updatePassword(@RequestBody User user) {
        return loginService.updateUser(user);
    }

    @PostMapping("/user/delete")
    @PreAuthorize("hasAuthority('system:user:delete')")
    public ResponseResult deleteUser(@RequestBody User user) {
        return loginService.deleteUser(user);
    }
}
