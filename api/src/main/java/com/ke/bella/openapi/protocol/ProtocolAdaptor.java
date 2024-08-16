package com.ke.bella.openapi.protocol;

import com.ke.bella.openapi.protocol.completion.CompletionRequest;
import com.ke.bella.openapi.protocol.completion.CompletionResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import okhttp3.Request;

public interface ProtocolAdaptor<T extends ProtocolAdaptor.BaseProperty> {

    String endpoint();

    Class<T> getPropertyClass();

    default Request.Builder authorizationRequestBuilder(AuthorizationType type, BaseProperty property) {
        Request.Builder builder = new Request.Builder();
        if(type == null) {
            return builder;
        }
        switch (type) {
        case BASIC:
            return builder.header("Authorization", property.getApiKey());
        case CUSTOM:
            return builder.header(property.getHeader(), property.getApiKey());
        default:
            return builder.header("Authorization", "Bearer " + property.getApiKey());
        }
    }


    interface CompletionAdaptor<T extends ProtocolAdaptor.BaseProperty> extends ProtocolAdaptor<T> {

        CompletionResponse httpRequest(CompletionRequest request, String url, T property);

        void streamRequest(CompletionRequest request, String url, T property, Callback.CompletionSseCallback callback);

        @Override
        default String endpoint() {
            return "/v1/chat/completions";
        }

    }

    interface EmbeddingAdaptor<T extends ProtocolAdaptor.BaseProperty> extends ProtocolAdaptor<T> {
        @Override
        default String endpoint() {
            return "/v1/embeddings";
        }
    }

    @Data
    class BaseProperty {
        private String apiKey;
        private String secret;
        private String header;
        private AuthorizationType authorizationType;
    }

    @Getter
    @AllArgsConstructor
    enum AuthorizationType {
        BASIC,
        BEARER,
        IAM,
        CUSTOM,
    }
}
