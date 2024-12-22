package org.ethanhao.triprover.service;

import jakarta.servlet.http.HttpServletResponse;
import org.ethanhao.triprover.domain.ResponseResult;
import org.ethanhao.triprover.domain.User;
import org.ethanhao.triprover.dto.user.UserAuthDTO;
import org.ethanhao.triprover.dto.user.UserRegisterDTO;
import org.ethanhao.triprover.dto.user.UserResponseDTO;
import org.ethanhao.triprover.dto.user.UserUpdateDTO;

public interface AuthService {
    void login(UserAuthDTO loginRequest, HttpServletResponse response);

    void logout(HttpServletResponse response);

    UserResponseDTO register(UserRegisterDTO registerRequest);

    UserResponseDTO updateUser(Long userId, UserUpdateDTO updateRequest);

    ResponseResult<Object> deleteUser(User user);
}
