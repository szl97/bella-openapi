package com.ke.bella.openapi.protocol;

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


    interface EmbeddingAdaptor extends IProtocolAdaptor {
        @Override
        default String endpoint() {
            return "/v1/embeddings";
        }
    }
}
