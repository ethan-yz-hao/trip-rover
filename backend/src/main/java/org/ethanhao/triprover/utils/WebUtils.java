package org.ethanhao.triprover.utils;

import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public class WebUtils {
    /**
     * Render string to client
     *
     * @param response HttpServletResponse
     * @param string   String to be rendered
     * @return null
     */
    public static String renderString(HttpServletResponse response, String
            string) {
        try {
            response.setStatus(200);
            response.setContentType("application/json");
            response.setCharacterEncoding("utf-8");
            response.getWriter().print(string);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}