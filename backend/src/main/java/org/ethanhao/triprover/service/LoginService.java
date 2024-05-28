package org.ethanhao.triprover.service;

import org.ethanhao.triprover.domain.ResponseResult;
import org.ethanhao.triprover.domain.User;

public interface LoginService {
    ResponseResult login(User user);

    ResponseResult logout();

    ResponseResult register(User user);

    ResponseResult updateUser(User user);

    ResponseResult deleteUser(User user);
}
