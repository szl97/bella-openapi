package com.ke.bella.openapi.intercept;

import com.ke.bella.openapi.BellaContext;
import com.ke.bella.openapi.common.EntityConstants;
import com.ke.bella.openapi.apikey.ApikeyInfo;
import com.ke.bella.openapi.configuration.OpenApiProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class TestEnvironmentInterceptor extends HandlerInterceptorAdapter {
    @Value("${spring.profiles.active}")
    private String profile;
    @Autowired
    private OpenApiProperties properties;
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if("test".equals(profile)) {
            ApikeyInfo apikey = BellaContext.getApikey();
            if(apikey.getOwnerType().equals(EntityConstants.SYSTEM) || properties.getManagers().containsValue(apikey.getCode())) {
                return true;
            }
            return false;
        }
        return true;
    }
}
