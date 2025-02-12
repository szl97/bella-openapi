package com.ke.bella.openapi.protocol.completion;

import com.ke.bella.openapi.protocol.completion.Callbacks.StreamCompletionCallback;
import com.ke.bella.openapi.utils.DateTimeUtils;
import com.ke.bella.openapi.utils.HttpUtils;
import com.ke.bella.openapi.utils.JacksonUtils;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component("OpenAICompletion")
public class OpenAIAdaptor implements CompletionAdaptor<OpenAIProperty> {

    private final Callbacks.SseEventConverter<StreamCompletionResponse> sseConverter = (id, event, str) -> {
        StreamCompletionResponse response = JacksonUtils.deserialize(str, StreamCompletionResponse.class);
        if(response != null) {
            response.setCreated(DateTimeUtils.getCurrentSeconds());
        }
        return response;
    };

    @Override
    public CompletionResponse completion(CompletionRequest request, String url, OpenAIProperty property) {
        Request httpRequest = buildRequest(request, url, property);
        CompletionResponse response = HttpUtils.httpRequest(httpRequest, CompletionResponse.class, (errorResponse, res) -> {
            if(errorResponse.getError() != null) {
                errorResponse.getError().setHttpCode(res.code());
            }
        });
        ResponseHelper.splitReasoningFromContent(response, property);
        response.setCreated(DateTimeUtils.getCurrentSeconds());
        return response;
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
        if(StringUtils.isNotEmpty(property.getApiVersion())) {
            url += property.getApiVersion();
        }
        request.setModel(property.getDeployName());
        Request.Builder builder = authorizationRequestBuilder(property.getAuth())
                .url(url)
                .post(RequestBody.create(JacksonUtils.serialize(request), MediaType.parse("application/json")));
        if(MapUtils.isNotEmpty(property.getExtraHeaders())) {
            property.getExtraHeaders().forEach(builder::addHeader);
        }
        return builder.build();
    }

    @Override
    public String getDescription() {
        return "OpenAI协议";
    }

    @Override
    public Class<OpenAIProperty> getPropertyClass() {
        return OpenAIProperty.class;
    }
}
