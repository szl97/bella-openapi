package com.ke.bella.openapi.protocol.completion;

import com.ke.bella.openapi.protocol.AuthorizationProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AliProperty extends CompletionProtocolProperty {
    AuthorizationProperty auth;
    String deployName;
}
