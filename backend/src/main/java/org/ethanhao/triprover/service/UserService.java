package org.ethanhao.triprover.service;

import org.ethanhao.triprover.dto.user.UserAuthDTO;
import org.ethanhao.triprover.dto.user.UserRegisterDTO;
import org.ethanhao.triprover.dto.user.UserResponseDTO;
import org.ethanhao.triprover.dto.user.UserUpdateDTO;

import jakarta.servlet.http.HttpServletResponse;

public interface UserService {

    void login(UserAuthDTO loginRequest, HttpServletResponse response);

    void logout(HttpServletResponse response);

    UserResponseDTO register(UserRegisterDTO registerRequest);

    UserResponseDTO getUser(Long userId);

    UserResponseDTO updateUser(Long userId, UserUpdateDTO updateRequest);

    void deleteUser(String username);

    void changePassword(String oldPassword, String newPassword);

    UserResponseDTO getUserInfo(Long userId);

}
