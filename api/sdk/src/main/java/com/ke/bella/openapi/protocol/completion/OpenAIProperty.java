package com.ke.bella.openapi.protocol.completion;

import com.ke.bella.openapi.protocol.AuthorizationProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OpenAIProperty extends CompletionProtocolProperty {
    AuthorizationProperty auth;
    String deployName;
    String apiVersion;
    boolean supportStreamOptions;
}
