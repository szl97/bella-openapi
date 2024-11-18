package com.ke.bella.openapi.intercept;

import com.google.common.collect.Lists;
import com.ke.bella.openapi.BellaContext;
import com.ke.bella.openapi.Operator;
import com.ke.bella.openapi.apikey.ApikeyInfo;
import com.ke.bella.openapi.configuration.OpenApiProperties;
import com.ke.bella.openapi.common.exception.ChannelException;
import com.ke.bella.openapi.login.context.ConsoleContext;
import com.ke.bella.openapi.service.ApikeyService;
import com.ke.bella.openapi.utils.MatchUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

import static com.ke.bella.openapi.intercept.ConcurrentStartInterceptor.ASYNC_REQUEST_MARKER;

@Component
public class AuthorizationInterceptor extends HandlerInterceptorAdapter {
    @Autowired
    private ApikeyService apikeyService;
    @Autowired
    private OpenApiProperties properties;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if(Boolean.TRUE.equals(request.getAttribute(ASYNC_REQUEST_MARKER))) {
            return true;
        }
        boolean hasPermission;
        String url = request.getRequestURI();
        Operator op = ConsoleContext.getOperatorIgnoreNull();
        if(op != null) {
            List<String> roles = Lists.newArrayList(properties.getLoginRoles());
            List<String> excludes = Lists.newArrayList(properties.getLoginExcludes());
            if(properties.getManagers().containsKey(op.getUserId())) {
                String akCode = properties.getManagers().get(op.getUserId());
                ApikeyInfo apikeyInfo = apikeyService.queryByCode(akCode, true);
                BellaContext.setApikey(apikeyInfo);
                hasPermission = apikeyInfo.hasPermission(url);
            } else {
                hasPermission = roles.stream().anyMatch(role -> MatchUtils.matchUrl(role, url))
                        && excludes.stream().noneMatch(exclude -> MatchUtils.matchUrl(exclude, url));
            }
        } else {
            String auth = request.getHeader(HttpHeaders.AUTHORIZATION);
            if(StringUtils.isEmpty(auth)) {
                throw new ChannelException.AuthorizationException("Invalid Authorization");
            }
            if(auth.startsWith("Bearer ")) {
                auth = auth.substring(7);
            }
            ApikeyInfo apikeyInfo = apikeyService.verify(auth);
            BellaContext.setApikey(apikeyInfo);
            hasPermission = apikeyInfo.hasPermission(url);
        }
        if(!hasPermission) {
            throw new ChannelException.AuthorizationException("没有操作权限");
        }
        return true;
    }
}
