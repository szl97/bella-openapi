package com.ke.bella.openapi.console;

import com.ke.bella.openapi.Operator;
import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.Map;

public class ConsoleContext {
    private static final ThreadLocal<Operator> operatorLocal = new ThreadLocal<>();

    public static Operator getOperator() {
        Operator userInfo = operatorLocal.get();
        Assert.notNull(userInfo, "userInfo is null");
        return userInfo;
    }

    public static void setOperator(Operator operator) {
        operatorLocal.set(operator);
    }

    public static void clearAll() {
        operatorLocal.remove();
    }

    public static Map<String, Object> snapshot() {
        Map<String, Object> map = new HashMap<>();
        map.put("user", operatorLocal.get());
        return map;
    }

    public static void replace(Map<String, Object> map) {
        operatorLocal.set((Operator) map.get("user"));
    }

    public static final Operator SYS = Operator.builder()
            .userId(0L).userName("system")
            .build();
}
