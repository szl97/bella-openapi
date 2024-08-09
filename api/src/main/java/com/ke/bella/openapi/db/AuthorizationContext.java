package com.ke.bella.openapi.db;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.Map;

/**
 * Author: Stan Sai Date: 2024/7/31 20:15 description:
 */
public class AuthorizationContext {
    private static final ThreadLocal<UserInfo> operatorLocal = new ThreadLocal<>();
    private static final ThreadLocal<String> apiKey = new ThreadLocal<>();

    public static UserInfo getUserInfo() {
        UserInfo userInfo = operatorLocal.get();
        Assert.notNull(userInfo, "userInfo is null");
        return userInfo;
    }

    public static void setUserInfo(UserInfo operator) {
        operatorLocal.set(operator);
    }

    public static void setSystemUser() {
        operatorLocal.set(UserInfo.builder()
                .userId(0L).userName("system")
                .build());
    }

    public static void clearAll() {
        operatorLocal.remove();
        apiKey.remove();
    }

    public static Map<String, Object> snapshot() {
        Map<String, Object> map = new HashMap<>();
        map.put("user", operatorLocal.get());
        map.put("ak", apiKey.get());
        return map;
    }

    public static void replace(Map<String, Object> map) {
        operatorLocal.set((UserInfo) map.get("user"));
        apiKey.set((String) map.get("ak"));
    }

    public static String getApiKey() {
        return apiKey.get();
    }

    public static void setApiKey(String ak) {
        apiKey.set(ak);
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class UserInfo {
        private Long userId;
        private String userName;
    }
}
