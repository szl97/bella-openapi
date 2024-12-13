package com.ke.bella.openapi.request;

import com.ke.bella.openapi.BellaContext;
import com.ke.bella.openapi.apikey.ApikeyInfo;
import com.ke.bella.openapi.client.OpenapiClient;
import com.ke.bella.openapi.common.exception.ChannelException;
import com.ke.bella.openapi.utils.EncryptUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.util.Assert;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Enumeration;
import java.util.UUID;

public class BellaRequestFilter extends OncePerRequestFilter {
    private final String serviceId;
    private final ApikeyValidateType apikeyValidateType;
    private final OpenapiClient openapiClient;

    public BellaRequestFilter(String serviceId) {
        this(serviceId, ApikeyValidateType.NONE, null);
    }

    public BellaRequestFilter(String serviceId, ApikeyValidateType apikeyValidateType, OpenapiClient client) {
        Assert.isTrue(StringUtils.isNotEmpty(serviceId), "serviceId不能为空");
        Assert.notNull(apikeyValidateType, "apikeyValidateType不能为空");
        Assert.isTrue(ApikeyValidateType.NONE == apikeyValidateType || client != null, "openapiClient不能为空");
        this.serviceId = serviceId;
        this.apikeyValidateType = apikeyValidateType;
        this.openapiClient = client;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            bellaRequestFilter(request, response);
            filterChain.doFilter(request, response);
        } finally {
            BellaContext.clearAll();
        }
    }

    protected void bellaRequestFilter(HttpServletRequest request, HttpServletResponse response) {
        Enumeration<String> headerNames = request.getHeaderNames();
        while(headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement().toUpperCase();
            if(headerName.startsWith("X-BELLA-")) {
                BellaContext.getHeaders().put(headerName, request.getHeader(headerName));
            }
        }
        if (apikeyValidateType != ApikeyValidateType.NONE) {
            String auth = request.getHeader(HttpHeaders.AUTHORIZATION);
            if(StringUtils.isEmpty(auth)) {
                throw new ChannelException.AuthorizationException("Authorization is empty");
            }
            ApikeyInfo apikeyInfo = verifyAuthHeader(auth, openapiClient);
            if(apikeyValidateType == ApikeyValidateType.CHECK_PERMISSION) {
                if(!apikeyInfo.hasPermission(request.getRequestURI())) {
                    throw new ChannelException.AuthorizationException("没有操作权限");
                }
            }
            BellaContext.setApikey(apikeyInfo);
        }
        String bellaTraceId = BellaContext.getTraceId();
        if(bellaTraceId == null) {
            bellaTraceId = BellaContext.generateTraceId(serviceId);
            BellaContext.getHeaders().put(BellaContext.BELLA_TRACE_HEADER, bellaTraceId);
        }
        response.addHeader(BellaContext.BELLA_TRACE_HEADER, bellaTraceId);
        String requestId = UUID.randomUUID().toString();
        BellaContext.getHeaders().put(BellaContext.BELLA_REQUEST_ID_HEADER, requestId);
        response.addHeader(BellaContext.BELLA_REQUEST_ID_HEADER, requestId);

    }

    protected ApikeyInfo verifyAuthHeader(String auth, OpenapiClient client) {
        String ak;
        if(auth.startsWith("Bearer ")) {
            ak = auth.substring(7);
        } else {
            throw new ChannelException.AuthorizationException("Authorization must start with Bearer");
        }
        ApikeyInfo info = client.whoami(ak);
        if(info == null) {
            String display = EncryptUtils.desensitizeByLength(auth);
            String displayAk = EncryptUtils.desensitize(ak);
            throw new ChannelException.AuthorizationException("api key不存在，请求的header为：" + display + ", apikey为：" + displayAk);
        }
        info.setApikey(ak);
        return info;
    }

    public enum ApikeyValidateType {
        NONE,
        VERIFY,
        CHECK_PERMISSION
    }
}
