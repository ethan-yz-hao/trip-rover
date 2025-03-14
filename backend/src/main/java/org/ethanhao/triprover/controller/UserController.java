package org.ethanhao.triprover.controller;

import java.util.List;

import org.ethanhao.triprover.domain.LoginUser;
import org.ethanhao.triprover.domain.ResponseResult;
import org.ethanhao.triprover.dto.user.ChangePasswordDTO;
import org.ethanhao.triprover.dto.user.UserAuthDTO;
import org.ethanhao.triprover.dto.user.UserRegisterDTO;
import org.ethanhao.triprover.dto.user.UserResponseDTO;
import org.ethanhao.triprover.dto.user.UserUpdateDTO;
import org.ethanhao.triprover.service.AvatarService;
import org.ethanhao.triprover.service.SearchService;
import org.ethanhao.triprover.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private AvatarService avatarService;

    @Autowired
    private SearchService searchService;

    @PostMapping("/login")
    public ResponseResult<Object> login(@Valid @RequestBody UserAuthDTO loginRequest, HttpServletResponse response) {
        userService.login(loginRequest, response);
        return new ResponseResult<>(HttpStatus.OK.value(), "Login successful");
    }

    @PostMapping("/logout")
    public ResponseResult<Object> logout(HttpServletResponse response) {
        userService.logout(response);
        return new ResponseResult<>(HttpStatus.OK.value(), "Logout successful");
    }

    @PostMapping("/register")
    public ResponseResult<UserResponseDTO> register(@Valid @RequestBody UserRegisterDTO registerRequest) {
        UserResponseDTO userResponseDTO = userService.register(registerRequest);
        return new ResponseResult<>(HttpStatus.OK.value(), "Register successful", userResponseDTO);
    }

    @GetMapping("/profile")
    @PreAuthorize("hasAuthority('user:all')")
    public ResponseResult<UserResponseDTO> getProfile(Authentication authentication) {
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        UserResponseDTO userResponseDTO = userService.getUser(loginUser.getUser().getId());
        return new ResponseResult<>(HttpStatus.OK.value(), "Profile retrieved successfully", userResponseDTO);
    }

    @DeleteMapping("/delete")
    @PreAuthorize("hasAuthority('user:all')")
    public ResponseResult<Object> deleteUser(Authentication authentication) {
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        userService.deleteUser(loginUser.getUser().getUserName());
        return new ResponseResult<>(HttpStatus.OK.value(), "User delete successful");
    }

    @PatchMapping("/update")
    @PreAuthorize("hasAuthority('user:all')")
    public ResponseResult<UserResponseDTO> updatePassword(@RequestBody UserUpdateDTO updateRequest,
            Authentication authentication) {
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        Long userId = loginUser.getUser().getId();
        UserResponseDTO userResponseDTO = userService.updateUser(userId, updateRequest);
        return new ResponseResult<>(HttpStatus.OK.value(), "User update successful", userResponseDTO);
    }

    @PostMapping("/change-password")
    @PreAuthorize("hasAuthority('user:all')")
    public ResponseResult<Object> changePassword(@RequestBody ChangePasswordDTO changePasswordDTO) {
        userService.changePassword(changePasswordDTO.getOldPassword(), changePasswordDTO.getNewPassword());
        return new ResponseResult<>(HttpStatus.OK.value(), "Password change successful");
    }

    @PostMapping("/avatar")
    @PreAuthorize("hasAuthority('user:all')")
    public ResponseResult<UserResponseDTO> uploadAvatar(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        Long userId = loginUser.getUser().getId();

        UserResponseDTO userResponseDTO = avatarService.uploadAvatar(file, userId);

        return new ResponseResult<>(
                HttpStatus.OK.value(),
                "Avatar uploaded successfully",
                userResponseDTO);
    }

    @DeleteMapping("/avatar")
    @PreAuthorize("hasAuthority('user:all')")
    public ResponseResult<UserResponseDTO> deleteAvatar(Authentication authentication) {
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        Long userId = loginUser.getUser().getId();

        UserResponseDTO userResponseDTO = avatarService.deleteAvatar(userId);

        return new ResponseResult<>(
                HttpStatus.OK.value(),
                "Avatar deleted successfully",
                userResponseDTO);
    }

    @GetMapping("/search")
    @PreAuthorize("hasAuthority('user:all')")
    public ResponseResult<List<UserResponseDTO>> searchUsers(
            @RequestParam String query) {

        List<UserResponseDTO> users = searchService.searchUsers(query);
        return new ResponseResult<>(
                HttpStatus.OK.value(),
                "Search successful",
                users);
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasAuthority('user:all')")
    public ResponseResult<UserResponseDTO> getUserInfo(@PathVariable Long userId) {
        UserResponseDTO userInfo = userService.getUserInfo(userId);
        return new ResponseResult<>(
                HttpStatus.OK.value(),
                "User info retrieved successfully",
                userInfo);
    }
}
