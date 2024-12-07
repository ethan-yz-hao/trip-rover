package org.ethanhao.triprover.service;

import jakarta.servlet.http.HttpServletResponse;
import org.ethanhao.triprover.domain.ResponseResult;
import org.ethanhao.triprover.domain.User;

public interface AuthService {
    ResponseResult<Object> login(User user, HttpServletResponse response);

    ResponseResult<Object> logout(HttpServletResponse response);

    ResponseResult<Object> register(User user);

    ResponseResult<Object> updateUser(User user);

    ResponseResult<Object> deleteUser(User user);
}
