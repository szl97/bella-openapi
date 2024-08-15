package com.ke.bella.openapi.intercept;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Author: Stan Sai Date: 2024/7/31 20:34 description:
 */
@Component
public class ManagerInterceptor extends HandlerInterceptorAdapter {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        //todo: 校验AuthorizationContext中的user是否有manager权限
        return true;
    }
}

