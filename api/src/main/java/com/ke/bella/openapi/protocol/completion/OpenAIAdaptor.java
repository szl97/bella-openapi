package com.ke.bella.openapi.protocol.completion;

import com.alibaba.fastjson.JSON;
import com.ke.bella.openapi.protocol.Callback;
import com.ke.bella.openapi.protocol.ProtocolAdaptor;
import com.ke.bella.openapi.utils.HttpUtils;
import com.ke.bella.openapi.utils.JacksonUtils;
import lombok.Data;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.springframework.stereotype.Component;

@Component
public class OpenAIAdaptor implements ProtocolAdaptor.CompletionAdaptor<OpenAIAdaptor.OpenAIProperty> {

    private CompletionSseListener.SseConverter sseConverter = str -> JacksonUtils.deserialize(str, StreamCompletionResponse.class);

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
        Request.Builder builder = authorizationRequestBuilder(property.getAuthorizationType(), property)
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
    public static class OpenAIProperty extends ProtocolAdaptor.BaseProperty {
        private String deployName;
        private String apiVersion;
    }
}
