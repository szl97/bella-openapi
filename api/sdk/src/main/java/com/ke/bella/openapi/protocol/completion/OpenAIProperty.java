package com.ke.bella.openapi.protocol.completion;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.ke.bella.openapi.protocol.AuthorizationProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OpenAIProperty extends CompletionProperty {
    AuthorizationProperty auth;
    String deployName;
    String apiVersion;
    boolean supportStreamOptions;
}
