package com.ke.bella.openapi.login.cors;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class CORSFilter implements Filter {
    public static final Integer ORDER = Integer.MIN_VALUE + 51;
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        if(request.getHeader("Origin") != null) {
            response.setHeader("Access-Control-Allow-Origin", request.getHeader("Origin"));
        } else if(request.getHeader("Referer") != null) {
            response.setHeader("Access-Control-Allow-Origin", request.getHeader("Referer"));
        } else {
            response.setHeader("Access-Control-Allow-Origin", "*");
        }
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Max-Age", "3600");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization, X-Requested-With, Cookie");
        response.setHeader("Access-Control-Expose-Headers", "X-Redirect-Login");
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }
        chain.doFilter(servletRequest, servletResponse);
    }
}
