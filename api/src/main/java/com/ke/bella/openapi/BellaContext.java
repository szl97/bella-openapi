package com.ke.bella.openapi;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.Maps;
import com.ke.bella.openapi.db.repo.ApikeyRoleRepo;
import com.ke.bella.openapi.utils.JacksonUtils;
import lombok.Data;
import org.springframework.util.Assert;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class BellaContext {
    private static final ThreadLocal<EndpointRequestInfo> endpointRequestInfo = new ThreadLocal<>();

    private static final ThreadLocal<ContentCachingRequestWrapper> requestCache = new ThreadLocal<>();

    private static final ThreadLocal<ApikeyInfo> akThreadLocal = new ThreadLocal<>();

    private static final ThreadLocal<String> requestIdThreadLocal = new ThreadLocal<>();

    private static EndpointRequestInfo getRequestInfo() {
        if(endpointRequestInfo.get() == null) {
            endpointRequestInfo.set(new EndpointRequestInfo());
        }
        return endpointRequestInfo.get();
    }

    public static void set(Attribute attribute, String value) {
        getRequestInfo().put(attribute, value);
    }

    public static String get(Attribute attribute) {
        Assert.isTrue(getRequestInfo().contains(attribute), attribute.name() + " is null");
        return getRequestInfo().get(attribute);
    }

    public static ContentCachingRequestWrapper getRequest() {
        Assert.notNull(requestCache.get(), "requestCache is empty");
        return requestCache.get();
    }

    public static void setRequest(ContentCachingRequestWrapper request) {
        requestCache.set(request);
    }

    public static String getRequestId() {
        Assert.notNull(requestIdThreadLocal.get(), "requestId is empty");
        return requestIdThreadLocal.get();
    }

    public static void setRequestId(String requestId) {
        requestIdThreadLocal.set(requestId);
    }

    public static ApikeyInfo getApikey() {
        Assert.notNull(akThreadLocal.get(), "ak is empty");
        return akThreadLocal.get();
    }

    public static void setApikey(ApikeyInfo ak) {
        akThreadLocal.set(ak);
    }

    public static void clearAll() {
        endpointRequestInfo.remove();
        requestCache.remove();
        requestIdThreadLocal.remove();
        akThreadLocal.remove();
    }

    public static Map<Attribute, String> getAllAttributes() {
        return Maps.newHashMap(getRequestInfo().map);
    }

    public enum Attribute {
        ENDPOINT, MODEL, ACCOUNT, USER
    }

    static class EndpointRequestInfo {
        Map<Attribute, String> map = new HashMap<>();

        private void put(Attribute key, String value) {
            map.put(key, value);
        }

        private String get(Attribute key) {
            return map.get(key);
        }

        private boolean contains(Attribute key) {
            return map.containsKey(key);
        }
    }

    @Data
    public static class ApikeyInfo {
        private String code;
        private String serviceId;
        private String akSha;
        private String parentCode;
        private String ownerType;
        private String ownerCode;
        private String ownerName;
        private String roleCode;
        @JsonIgnore
        private String path;
        private Byte safetyLevel;
        private BigDecimal monthQuota;
        private ApikeyRoleRepo.RolePath rolePath;
        private String status;
        public ApikeyRoleRepo.RolePath getRolePath() {
            if(path == null) {
                return new ApikeyRoleRepo.RolePath();
            }
            if(rolePath == null) {
                rolePath = JacksonUtils.deserialize(path, ApikeyRoleRepo.RolePath.class);
            }
            return rolePath;
        }
    }

}
