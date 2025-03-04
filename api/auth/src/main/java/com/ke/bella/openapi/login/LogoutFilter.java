package com.ke.bella.openapi.login;

import com.ke.bella.openapi.login.session.SessionManager;
import org.springframework.http.HttpStatus;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class LogoutFilter implements Filter {
    private final SessionManager sessionManager;

    public LogoutFilter(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        if (!"/logout".equals(httpRequest.getRequestURI())) {
            chain.doFilter(request, response);
            return;
        }
        sessionManager.destroySession(httpRequest, httpResponse);
        httpResponse.setStatus(HttpStatus.OK.value());
    }
}
