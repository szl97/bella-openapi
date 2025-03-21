package com.ke.bella.openapi.login;

import com.ke.bella.openapi.BellaResponse;
import com.ke.bella.openapi.Operator;
import com.ke.bella.openapi.login.session.SessionManager;
import com.ke.bella.openapi.utils.JacksonUtils;
import org.springframework.http.HttpStatus;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class LoginFilter implements Filter {
    private final SessionManager sessionManager;

    public LoginFilter(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;


        if ("/logout".equals(httpRequest.getRequestURI())) {
            sessionManager.destroySession(httpRequest, httpResponse);
            httpResponse.setStatus(HttpStatus.OK.value());
            return;
        }
        if("/userInfo".equals(httpRequest.getRequestURI())) {
            Operator operator = sessionManager.getSession(httpRequest);
            BellaResponse<Operator> bellaResponse = new BellaResponse<>();
            bellaResponse.setCode(operator == null ? 401 : 200);
            bellaResponse.setData(operator);
            response.setContentType("application/json");
            response.getWriter().write(JacksonUtils.serialize(bellaResponse));
            return;
        }
        chain.doFilter(request, response);
    }
}
