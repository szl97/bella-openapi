package com.ke.bella.openapi.script;

public enum ScriptType {
    metrics;

    public String getScriptName(String endpoint) {
        return this.name() + endpoint;
    }
}
