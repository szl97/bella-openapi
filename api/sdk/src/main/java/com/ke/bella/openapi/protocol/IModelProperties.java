package com.ke.bella.openapi.protocol;

import com.ke.bella.openapi.IDescription;
import com.ke.bella.openapi.protocol.completion.CompletionModelProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;

public interface IModelProperties extends IDescription {

    @AllArgsConstructor
    @Getter
    enum EndpointModelPropertyType {
        COMPLETION("/v1/chat/completions", CompletionModelProperties.class),
        ;

        private final String endpoint;
        private final Class<? extends IModelProperties> type;

        public static Class<? extends IModelProperties> fetchType(String endpoint) {
            for (EndpointModelPropertyType t : EndpointModelPropertyType.values()) {
                if (t.endpoint.equals(endpoint)) {
                    return t.type;
                }
            }
            return null;
        }

    }
}
