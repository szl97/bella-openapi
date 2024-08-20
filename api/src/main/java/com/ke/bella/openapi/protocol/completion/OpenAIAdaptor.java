package com.ke.bella.openapi.protocol.completion;

import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.ke.bella.openapi.protocol.AuthorizationProperty;
import com.ke.bella.openapi.protocol.IProtocolProperty;
import com.ke.bella.openapi.protocol.IProtocolAdaptor;
import com.ke.bella.openapi.utils.HttpUtils;
import com.ke.bella.openapi.utils.JacksonUtils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;

@Component("OpenAICompletion")
public class OpenAIAdaptor implements IProtocolAdaptor.CompletionAdaptor<OpenAIAdaptor.OpenAIProperty> {

    private final Callback.SseConvertCallback<String> sseConverter = str -> JacksonUtils.deserialize(str, StreamCompletionResponse.class);

    @Override
    public CompletionResponse httpRequest(CompletionRequest request, String url, OpenAIProperty property) {
        Request httpRequest = buildRequest(request, url, property);
        return HttpUtils.httpRequest(httpRequest, CompletionResponse.class);
    }

    @Override
    public void streamRequest(CompletionRequest request, String url, OpenAIProperty property, Callback.CompletionSseCallback callback) {
        Request httpRequest = buildRequest(request, url, property);
        HttpUtils.streamRequest(httpRequest, new CompletionSseListener(callback, sseConverter));
    }

    private Request buildRequest(CompletionRequest request, String url, OpenAIProperty property) {
        if(property.getApiVersion() != null) {
            url += property.getApiVersion();
        }
        request.setModel(property.getDeployName());
        Request.Builder builder = authorizationRequestBuilder(property.getAuth())
                .url(url)
                .post(RequestBody.create(MediaType.parse("application/json"),
                        JSON.toJSONString(request)));
        return builder.build();
    }

    @Override
    public Class<OpenAIProperty> getPropertyClass() {
        return OpenAIProperty.class;
    }

    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OpenAIProperty implements IProtocolProperty {
        AuthorizationProperty auth;
        String deployName;
        String apiVersion;
    }
}
