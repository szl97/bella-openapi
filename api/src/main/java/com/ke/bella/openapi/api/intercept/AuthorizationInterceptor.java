package com.ke.bella.openapi.api.intercept;

import com.ke.bella.openapi.BellaContext;
import com.ke.bella.openapi.BellaContext.Operator;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class AuthorizationInterceptor extends HandlerInterceptorAdapter {

    @Value("${spring.profiles.active}")
    private String profile;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        //todo: ak鉴权，信息加入AuthorizationContext
        if(profile.equals("dev")) {
            BellaContext.setOperator(Operator.SYS);
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        BellaContext.clearAll();
    }
}
