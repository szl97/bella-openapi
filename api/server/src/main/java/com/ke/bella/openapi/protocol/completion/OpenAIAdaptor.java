package com.ke.bella.openapi.protocol.completion;

import com.alibaba.fastjson.JSON;
import com.ke.bella.openapi.protocol.completion.Callbacks.StreamCompletionCallback;
import com.ke.bella.openapi.utils.DateTimeUtils;
import com.ke.bella.openapi.utils.HttpUtils;
import com.ke.bella.openapi.utils.JacksonUtils;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.springframework.stereotype.Component;

@Component("OpenAICompletion")
public class OpenAIAdaptor implements CompletionAdaptor<OpenAIProperty> {

    private final Callbacks.SseEventConverter<StreamCompletionResponse> sseConverter = (id, event, str) -> {
        StreamCompletionResponse response = JacksonUtils.deserialize(str, StreamCompletionResponse.class);
        if(response != null && response.getCreated() == 0) {
            response.setCreated(DateTimeUtils.getCurrentMills());
        }
        return response;
    };

    @Override
    public CompletionResponse completion(CompletionRequest request, String url, OpenAIProperty property) {
        Request httpRequest = buildRequest(request, url, property);
        return HttpUtils.httpRequest(httpRequest, CompletionResponse.class);
    }

    @Override
    public void streamCompletion(CompletionRequest request, String url, OpenAIProperty property, StreamCompletionCallback callback) {
        if(property.supportStreamOptions) {
            request.setStream_options(new CompletionRequest.StreamOptions());
        }
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
                .post(RequestBody.create(JSON.toJSONString(request), MediaType.parse("application/json")));
        return builder.build();
    }

    @Override
    public Class<OpenAIProperty> getPropertyClass() {
        return OpenAIProperty.class;
    }
}
