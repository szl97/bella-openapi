package com.ke.bella.openapi.login;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import com.ke.bella.openapi.BellaResponse;
import com.ke.bella.openapi.Operator;
import com.ke.bella.openapi.login.session.SessionManager;
import com.ke.bella.openapi.utils.JacksonUtils;

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
        if("/login".equals(httpRequest.getRequestURI()) && httpRequest.getMethod().equalsIgnoreCase(HttpMethod.POST.name()) ) {
            if(!sessionManager.userRepoInitialized()) {
                BellaResponse<Boolean> bellaResponse = new BellaResponse<>();
                bellaResponse.setCode(503);
                bellaResponse.setMessage("未实现密钥登录功能");
                response.setContentType("application/json");
                response.getWriter().write(JacksonUtils.serialize(bellaResponse));
                return;
            }
            String jsonBody = readRequestBody(httpRequest);
            if(jsonBody != null) {
                Map<String, Object> map = JacksonUtils.toMap(jsonBody);
                if(map.get("secret") != null) {
                    String id = sessionManager.create(map.get("secret").toString(), httpRequest, httpResponse);
                    if(id != null) {
                        BellaResponse<Boolean> bellaResponse = new BellaResponse<>();
                        bellaResponse.setCode(200);
                        bellaResponse.setData(true);
                        response.setContentType("application/json");
                        response.getWriter().write(JacksonUtils.serialize(bellaResponse));
                        return;
                    }
                }
            }
            BellaResponse<Boolean> bellaResponse = new BellaResponse<>();
            bellaResponse.setCode(400);
            bellaResponse.setMessage("登录密钥错误");
            response.setContentType("application/json");
            response.getWriter().write(JacksonUtils.serialize(bellaResponse));
            return;
        }
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

    private String readRequestBody(HttpServletRequest request) throws IOException {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(request.getInputStream(), StandardCharsets.UTF_8))) {
            return reader.lines().collect(Collectors.joining());
        }
    }
}
