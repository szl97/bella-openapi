package com.ke.bella.openapi.protocol.completion;

import com.ke.bella.openapi.protocol.IModelProperties;
import lombok.Data;

@Data
public class CompletionModelProperties implements IModelProperties {
    private int max_input_context;
    private int max_output_context;
}
