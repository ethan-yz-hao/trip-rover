package org.ethanhao.triprover.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.ethanhao.triprover.domain.LoginUser;
import org.ethanhao.triprover.domain.ResponseResult;
import org.ethanhao.triprover.utils.JwtUtil;
import org.ethanhao.triprover.utils.RedisCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RedisCache redisCache;

    @Value("${JWT_TTL}")
    private Long jwtTtl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();

        String userId = loginUser.getUser().getId().toString();
        // Create a JWT token and store user session in Redis
        String jwtToken = jwtUtil.createJWT(userId, jwtTtl);
        redisCache.setCacheObject("login:" + userId, loginUser);

//        Map<String, String> responseMap = new HashMap<>();
//        responseMap.put("token", jwtToken);
//        ResponseResult result = new ResponseResult(HttpStatus.OK.value(), "OAuth2 login successful", responseMap);

//        // Send the result to the client
//        response.setContentType("application/json;charset=UTF-8");
//        response.getWriter().write(result.toString());

//        // Clear the default redirect behavior
//        clearAuthenticationAttributes(request);

//         redirect to the home page (Header needed for authentication to work properly)
        response.setHeader("Authorization", jwtToken);

        response.sendRedirect("/hello");


        // authenticate the user
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
