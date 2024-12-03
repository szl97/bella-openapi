package com.ke.bella.openapi.script;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ScriptType {
    metrics("metrics"),
    metricsQuery("metrics/query");

    final String path;
    public String getScriptName(String endpoint) {
        return this.path + endpoint;
    }
}
