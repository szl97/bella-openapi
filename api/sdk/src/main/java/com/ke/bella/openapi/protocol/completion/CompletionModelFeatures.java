package com.ke.bella.openapi.protocol.completion;

import com.ke.bella.openapi.protocol.IModelFeatures;
import lombok.Data;

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
}
