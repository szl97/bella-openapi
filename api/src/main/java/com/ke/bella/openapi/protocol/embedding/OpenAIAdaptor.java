package com.ke.bella.openapi.protocol.embedding;

import org.springframework.stereotype.Component;

import com.ke.bella.openapi.protocol.AuthorizationProperty;
import com.ke.bella.openapi.protocol.IProtocolAdaptor;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Component("OpenAIEmbedding")
public class OpenAIAdaptor implements IProtocolAdaptor.EmbeddingAdaptor {

    @Override
    public Class<?> getPropertyClass() {
        return OpenAIProperty.class;
    }

    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OpenAIProperty {
        AuthorizationProperty auth;
    }
}
