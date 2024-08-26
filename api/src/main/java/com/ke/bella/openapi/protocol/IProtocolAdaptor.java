package com.ke.bella.openapi.protocol;

import com.ke.bella.openapi.protocol.completion.Callbacks;
import com.ke.bella.openapi.protocol.completion.CompletionRequest;
import com.ke.bella.openapi.protocol.completion.CompletionResponse;

import okhttp3.Request;

public interface IProtocolAdaptor {

    String endpoint();

    Class<?> getPropertyClass();

    default Request.Builder authorizationRequestBuilder(AuthorizationProperty property) {
        Request.Builder builder = new Request.Builder();
        if(property == null) {
            return builder;
        }
        switch (property.type) {
        case BASIC:
            return builder.header("Authorization", property.getApiKey());
        case CUSTOM:
            return builder.header(property.getHeader(), property.getApiKey());
        default:
            return builder.header("Authorization", "Bearer " + property.getApiKey());
        }
    }


    interface CompletionAdaptor<T extends IProtocolProperty> extends IProtocolAdaptor {

        CompletionResponse httpRequest(CompletionRequest request, String url, T property);

        void streamRequest(CompletionRequest request, String url, T property, Callbacks.StreamCompletionCallback callback);

        @Override
        default String endpoint() {
            return "/v1/chat/completions";
        }

    }

    interface EmbeddingAdaptor extends IProtocolAdaptor {
        @Override
        default String endpoint() {
            return "/v1/embeddings";
        }
    }
}
