package com.ke.bella.openapi.safety;

public interface ISafetyCheckService<T extends SafetyCheckRequest> {
    Object safetyCheck(T request);
}
