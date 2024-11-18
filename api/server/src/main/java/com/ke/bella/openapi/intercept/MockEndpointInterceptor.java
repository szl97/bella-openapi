package com.ke.bella.openapi.intercept;

import com.ke.bella.openapi.BellaContext;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class MockEndpointInterceptor extends HandlerInterceptorAdapter {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if("true".equals(request.getHeader("X-BELLA-MOCK-REQUEST"))) {
            BellaContext.getProcessData().setMock(true);
        }
        return true;
    }
}
