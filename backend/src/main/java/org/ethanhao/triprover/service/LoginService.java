package org.ethanhao.triprover.service;

import org.ethanhao.triprover.domain.ResponseResult;
import org.ethanhao.triprover.domain.User;

public interface LoginService {
    ResponseResult login(User user);
}
