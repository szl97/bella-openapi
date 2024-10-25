package com.ke.bella.openapi.login.cas;

import com.ke.bella.openapi.Operator;
import com.ke.bella.openapi.console.ConsoleContext;
import com.ke.bella.openapi.login.session.SessionManager;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class BellaAuthenticationFilter implements Filter {
    private final String loginUrl;
    private final String clientHost;
    private final String serviceUri;
    private final boolean clientSupport;
    private final String clientIndex;
    private final String logoutUri;
    private final SessionManager sessionManager;

    public BellaAuthenticationFilter(String loginUrl, String clientHost, String serviceUri, boolean clientSupport, String clientIndex,
            String logoutUri, SessionManager sessionManager) {
        this.loginUrl = loginUrl;
        this.clientHost = clientHost;
        this.serviceUri = serviceUri;
        this.clientSupport = clientSupport;
        this.clientIndex = clientIndex;
        this.logoutUri = logoutUri;
        this.sessionManager = sessionManager;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        if(httpRequest.getRequestURI().equals(serviceUri)) {
            //不处理cas服务的登出请求
            return;
        }
        if(httpRequest.getRequestURI().equals(logoutUri)) {
            sessionManager.destroySession(httpRequest);
            return;
        }
        String auth = httpRequest.getHeader(HttpHeaders.AUTHORIZATION);
        if(StringUtils.isNotBlank(auth)) {
            chain.doFilter(request, response);
            return;
        }
        Operator operator = sessionManager.getSession(httpRequest);
        if(operator == null) {
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            StringBuilder loginBuilder = new StringBuilder(loginUrl);
            loginBuilder.append("?service=");
            StringBuilder serviceBuilder = new StringBuilder(clientHost).append(serviceUri).append("?")
                    .append(BellaCasClient.redirectParameter).append("=");
            if(!clientSupport) {
                serviceBuilder.append(clientIndex);
            }
            String encodedService = URLEncoder.encode(serviceBuilder.toString(), StandardCharsets.UTF_8.toString());
            loginBuilder.append(encodedService);
            if(clientSupport) {
                httpResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
                httpResponse.setHeader("X-Redirect-Login", loginBuilder.toString());
            } else {
                httpResponse.sendRedirect(loginBuilder.toString());
            }
            return;
        }
        try {
            ConsoleContext.setOperator(operator);
            chain.doFilter(request, response);
        } finally {
            ConsoleContext.clearAll();
            sessionManager.renew(httpRequest);
        }
    }
}
