package org.ethanhao.triprover.handler;

import com.alibaba.fastjson.JSON;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.ethanhao.triprover.domain.ResponseResult;
import org.ethanhao.triprover.utils.WebUtils;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class SecurityFilterExceptionHandler implements AccessDeniedHandler, AuthenticationEntryPoint {
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                      AccessDeniedException accessDeniedException) throws IOException, ServletException {
        handleResponse(response, HttpStatus.FORBIDDEN, accessDeniedException.getMessage()); 
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                        AuthenticationException authException) throws IOException, ServletException {

        handleResponse(response, HttpStatus.UNAUTHORIZED, authException.getMessage());
    }

    private void handleResponse(HttpServletResponse response, HttpStatus status, 
                                     String message) throws IOException {
        ResponseResult<Object> result = new ResponseResult<>(status.value(), message);
        String json = JSON.toJSONString(result);
        WebUtils.renderString(response, status.value(), json);
    }
}
