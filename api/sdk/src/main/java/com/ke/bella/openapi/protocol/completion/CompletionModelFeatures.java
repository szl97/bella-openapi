package com.ke.bella.openapi.protocol.completion;

import com.google.common.collect.ImmutableSortedMap;
import com.ke.bella.openapi.protocol.IModelFeatures;
import lombok.Data;

import java.util.LinkedHashMap;
import java.util.Map;

@Data
public class CompletionModelFeatures implements IModelFeatures {
    private boolean stream;
    private boolean function_call;
    private boolean stream_function_call;
    private boolean parallel_tool_calls;
    private boolean vision;
    private boolean json_format;
    private boolean json_schema;
    private boolean agent_thought;

    @Override
    public Map<String, String> description() {
        Map<String, String> desc = new LinkedHashMap<>();
        desc.put("stream", "是否支持流式输出");
        desc.put("function_call", "是否支持函数调用");
        desc.put("stream_function_call", "是否支持函数调用的流式输出");
        desc.put("parallel_tool_calls", "是否支持并行工具调用");
        desc.put("vision", "是否支持视觉");
        desc.put("json_format", "是否支持JSON格式的输出");
        desc.put("json_schema", "是否支持JSON Schema的输出");
        desc.put("agent_thought", "是否支持Agent thought的输出");
        return desc;
    }
}
