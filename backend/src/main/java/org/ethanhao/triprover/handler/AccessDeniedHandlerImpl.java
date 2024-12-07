package org.ethanhao.triprover.handler;

import com.alibaba.fastjson.JSON;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.ethanhao.triprover.domain.ResponseResult;
import org.ethanhao.triprover.utils.WebUtils;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class AccessDeniedHandlerImpl implements AccessDeniedHandler {
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException, ServletException {
        if (isWebSocketHandshake(request)) {
            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.setContentType("text/plain");
            response.getWriter().write("Authorization failed, insufficient permissions");
            response.getWriter().flush();
        } else {
            ResponseResult<Object> result = new ResponseResult<>(HttpStatus.FORBIDDEN.value(), "Authorization failed, insufficient permissions");
            String json = JSON.toJSONString(result);
            WebUtils.renderString(response, HttpStatus.FORBIDDEN.value(), json);
        }
    }

    private boolean isWebSocketHandshake(HttpServletRequest request) {
        String upgradeHeader = request.getHeader("Upgrade");
        return "websocket".equalsIgnoreCase(upgradeHeader);
    }
}
