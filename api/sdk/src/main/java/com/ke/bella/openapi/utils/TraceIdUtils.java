package com.ke.bella.openapi.utils;

import java.util.UUID;

public class TraceIdUtils {
    public static String generateTraceId(String serviceId) {
        return serviceId + "-" + UUID.randomUUID();
    }
}
