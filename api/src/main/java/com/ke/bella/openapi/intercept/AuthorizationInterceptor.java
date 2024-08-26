package com.ke.bella.openapi.intercept;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.ke.bella.openapi.BellaContext;
import com.ke.bella.openapi.protocol.ChannelException;
import com.ke.bella.openapi.service.ApikeyService;
import com.ke.bella.openapi.utils.MatchUtils;

@Component
public class AuthorizationInterceptor extends HandlerInterceptorAdapter {
    @Autowired
    private ApikeyService apikeyService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String auth = request.getHeader(HttpHeaders.AUTHORIZATION);
        if(StringUtils.isEmpty(auth) || !auth.startsWith("Bearer ")) {
            throw new ChannelException.AuthorizationException("Invalid Authorization");
        }
        String ak = auth.substring(7);
        BellaContext.ApikeyInfo apikeyInfo = apikeyService.verify(ak);
        String url = request.getRequestURI();
        boolean match = apikeyInfo.getRolePath().getIncluded().stream().anyMatch(pattern -> MatchUtils.mathUrl(pattern, url))
                && apikeyInfo.getRolePath().getExcluded().stream().noneMatch(pattern -> MatchUtils.mathUrl(pattern, url));
        if(!match) {
            throw new ChannelException.AuthorizationException("没有操作权限");
        }
        BellaContext.setApikey(apikeyInfo);
        return true;
    }
}
