package com.ke.bella.openapi.protocol.embedding;

import com.ke.bella.openapi.protocol.ProtocolAdaptor;
import org.springframework.stereotype.Component;

/**
 * Author: Stan Sai Date: 2024/8/15 10:01 description:
 */
@Component
public class OpenAIAdaptor implements ProtocolAdaptor.EmbeddingAdaptor<OpenAIAdaptor.OpenAIProperty> {

    @Override
    public Class<OpenAIProperty> getPropertyClass() {
        return null;
    }

    public static class OpenAIProperty extends ProtocolAdaptor.BaseProperty {

    }
}
