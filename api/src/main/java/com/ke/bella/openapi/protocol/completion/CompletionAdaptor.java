package com.ke.bella.openapi.protocol.completion;

import com.ke.bella.openapi.protocol.IProtocolAdaptor;
import com.ke.bella.openapi.protocol.IProtocolProperty;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

public interface CompletionAdaptor<T extends CompletionAdaptor.CompletionProperty> extends IProtocolAdaptor {

    CompletionResponse completion(CompletionRequest request, String url, T property);

    void streamCompletion(CompletionRequest request, String url, T property, Callbacks.StreamCompletionCallback callback);

    @Override
    default String endpoint() {
        return "/v1/chat/completions";
    }

    @Data
    class CompletionProperty implements IProtocolProperty {
        String encodingType = StringUtils.EMPTY;
    }
}
