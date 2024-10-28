package com.ke.bella.openapi.intercept;

import com.google.common.collect.Lists;
import com.ke.bella.openapi.BellaContext;
import com.ke.bella.openapi.Operator;
import com.ke.bella.openapi.apikey.ApikeyInfo;
import com.ke.bella.openapi.configuration.OpenApiProperties;
import com.ke.bella.openapi.console.ConsoleContext;
import com.ke.bella.openapi.protocol.ChannelException;
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
        String url = request.getRequestURI();
        Operator op = ConsoleContext.getOperatorIgnoreNull();
        if(op != null) {
            List<String> roles = Lists.newArrayList(properties.getLoginRoles());
            if(properties.getManagers().containsKey(op.getUserId())) {
                String akCode = properties.getManagers().get(op.getUserId());
                ApikeyInfo apikeyInfo = apikeyService.queryByCode(akCode, true);
                BellaContext.setApikey(apikeyInfo);
                roles.add("/console/**");
            }
            if(roles.stream().anyMatch(role -> MatchUtils.matchUrl(role, url))) {
                return true;
            }
        }
        String auth = request.getHeader(HttpHeaders.AUTHORIZATION);
        if(StringUtils.isEmpty(auth) || !auth.startsWith("Bearer ")) {
            throw new ChannelException.AuthorizationException("Invalid Authorization");
        }
        String ak = auth.substring(7);
        ApikeyInfo apikeyInfo = apikeyService.verify(ak);
        boolean match = apikeyInfo.getRolePath().getIncluded().stream().anyMatch(pattern -> MatchUtils.matchUrl(pattern, url))
                && apikeyInfo.getRolePath().getExcluded().stream().noneMatch(pattern -> MatchUtils.matchUrl(pattern, url));
        if(!match) {
            throw new ChannelException.AuthorizationException("没有操作权限");
        }
        BellaContext.setApikey(apikeyInfo);
        return true;
    }
}
