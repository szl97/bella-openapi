package com.ke.bella.openapi.controller.intercept;

import com.ke.bella.openapi.db.RequestInfoContext;
import org.apache.commons.lang3.StringUtils;
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
            String requestId = request.getHeader("requestId");
            if(StringUtils.isBlank(requestId)) {
                requestId = UUID.randomUUID().toString();
            }
            RequestInfoContext.setRequestId(requestId);
            ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);
            RequestInfoContext.setRequest(wrappedRequest);
            chain.doFilter(wrappedRequest, response);
        } finally {
            RequestInfoContext.clearAll();
        }
    }
}
