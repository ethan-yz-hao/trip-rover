package org.ethanhao.triprover.handler;

import com.alibaba.fastjson.JSON;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.ethanhao.triprover.domain.ResponseResult;
import org.ethanhao.triprover.utils.WebUtils;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class AuthenticationEntryPointImpl implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {
        ResponseResult<Object> result = new
                ResponseResult<>(HttpStatus.UNAUTHORIZED.value(), "Authentication failed, please log in again");
        String json = JSON.toJSONString(result);
        WebUtils.renderString(response, json);
    }
}
