package com.ke.bella.openapi.protocol;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class AuthorizationProperty implements IProtocalProperty {
    @Getter
    @AllArgsConstructor
    public enum AuthType {
        BASIC,
        BEARER,
        IAM,
        CUSTOM,
    }

    AuthType type;
    String header;
    String apiKey;
    String secret;
}
