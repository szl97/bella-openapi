package com.ke.bella.openapi.intercept;

import com.ke.bella.openapi.BellaContext;
import com.ke.bella.openapi.apikey.ApikeyInfo;
import com.ke.bella.openapi.exception.ChannelException;
import com.ke.bella.openapi.service.ApikeyService;
import com.ke.bella.openapi.utils.DateTimeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;

import static com.ke.bella.openapi.intercept.ConcurrentStartInterceptor.ASYNC_REQUEST_MARKER;

@Component
public class MonthQuotaInterceptor extends HandlerInterceptorAdapter {
    @Autowired
    private ApikeyService apikeyService;
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (Boolean.TRUE.equals(request.getAttribute(ASYNC_REQUEST_MARKER))) {
            return true;
        }
        ApikeyInfo apikey = BellaContext.getApikey();
        BigDecimal cost = apikeyService.loadCost(apikey.getCode(), DateTimeUtils.getCurrentMonth());
        if(apikey.getMonthQuota().doubleValue() <= cost.doubleValue()) {
            throw new ChannelException.RateLimitException("已达每月额度上限");
        }
        return true;
    }
}
