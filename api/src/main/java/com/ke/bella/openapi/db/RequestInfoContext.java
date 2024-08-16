package com.ke.bella.openapi.db;

import com.google.common.collect.Maps;
import org.springframework.util.Assert;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.util.HashMap;
import java.util.Map;

public class RequestInfoContext {
    private static final ThreadLocal<EndpointRequestInfo> endpointRequestInfo = new ThreadLocal<>();

    private static final ThreadLocal<ContentCachingRequestWrapper> requestCache = new ThreadLocal<>();

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
        Assert.isTrue(getRequestInfo().contains(attribute), attribute + " is null");
        return getRequestInfo().get(attribute);
    }

    public static ContentCachingRequestWrapper getRequest() {
        Assert.notNull(requestCache.get(), requestCache + " is empty");
        return requestCache.get();
    }

    public static void setRequest(ContentCachingRequestWrapper request) {
        requestCache.set(request);
    }

    public static String getRequestId() {
        Assert.notNull(requestIdThreadLocal.get(), requestIdThreadLocal + " is empty");
        return requestIdThreadLocal.get();
    }

    public static void setRequestId(String requestId) {
        requestIdThreadLocal.set(requestId);
    }

    public static void clearAll() {
        endpointRequestInfo.remove();
        requestCache.remove();
        requestIdThreadLocal.remove();
    }

    public static Map<Attribute, String> getAllAttributes() {
        return Maps.newHashMap(getRequestInfo().map);
    }

    public enum Attribute {
        ENDPOINT, MODEL, ACCOUNT, USER, DATA_PERMISSION
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

}
