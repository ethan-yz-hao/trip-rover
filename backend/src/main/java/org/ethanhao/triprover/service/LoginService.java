package org.ethanhao.triprover.service;

import jakarta.servlet.http.HttpServletResponse;
import org.ethanhao.triprover.domain.ResponseResult;
import org.ethanhao.triprover.domain.User;

public interface LoginService {
    ResponseResult login(User user, HttpServletResponse response);

    ResponseResult logout(HttpServletResponse response);

    ResponseResult register(User user);

    ResponseResult updateUser(User user);

    ResponseResult deleteUser(User user);
}
