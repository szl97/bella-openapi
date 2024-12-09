package com.ke.bella.openapi.intercept;

import com.ke.bella.openapi.BellaContext;
import com.ke.bella.openapi.login.context.ConsoleContext;
import com.ke.bella.openapi.utils.DateTimeUtils;
import com.ke.bella.openapi.utils.TraceIdUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

/**
 * Author: Stan Sai Date: 2024/8/13 16:50 description:
 */
@Component
public class OpenapiRequestFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        try {
            String requestId = UUID.randomUUID().toString();
            BellaContext.getProcessData().setRequestId(requestId);
            BellaContext.getProcessData().setRequestTime(DateTimeUtils.getCurrentSeconds());
            ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);
            BellaContext.setRequest(wrappedRequest);
            response.addHeader("X-BELLA-REQUEST-ID", requestId);
            String bellaTraceId = request.getHeader(TraceIdUtils.headerName());
            if (bellaTraceId != null) {
                BellaContext.getProcessData().setBellaTraceId(bellaTraceId);
                response.addHeader(TraceIdUtils.headerName(), bellaTraceId);
            }
            chain.doFilter(wrappedRequest, response);
        } finally {
            ConsoleContext.clearAll();
            BellaContext.clearAll();
        }
    }
}
