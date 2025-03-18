package com.ke.bella.openapi.protocol.tts;

import java.io.OutputStream;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.ke.bella.openapi.EndpointProcessData;
import com.ke.bella.openapi.protocol.Callbacks;
import com.ke.bella.openapi.protocol.log.EndpointLogger;
import com.ke.bella.openapi.utils.HttpUtils;
import com.ke.bella.openapi.utils.JacksonUtils;

import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;

import javax.servlet.AsyncContext;

@Component("OpenAITts")
public class OpenAIAdaptor implements TtsAdaptor<OpenAIProperty> {
    @Override
    public byte[] tts(TtsRequest request, String url, OpenAIProperty property) {
        Request httpRequest = buildRequest(request, url, property);
        return HttpUtils.doHttpRequest(httpRequest);
    }

    @Override
    public void streamTts(TtsRequest request, String url, OpenAIProperty property, Callbacks.StreamTtsCallback callback) {
        Request httpRequest = buildRequest(request, url, property);
        HttpUtils.streamRequest(httpRequest, new TtsStreamListener((Callbacks.HttpStreamTtsCallback) callback));
    }

    @Override
    public Callbacks.StreamTtsCallback buildCallback(TtsRequest request, OutputStream outputStream, AsyncContext context,
            EndpointProcessData processData, EndpointLogger logger) {
        return new OpenAIStreamTtsCallback(outputStream, context, processData, logger);
    }

    @Override
    public String getDescription() {
        return "OpenAI协议";
    }

    @Override
    public Class<?> getPropertyClass() {
        return OpenAIProperty.class;
    }

    private Request buildRequest(TtsRequest request, String url, OpenAIProperty property) {
        if(StringUtils.isNotBlank(property.deployName)) {
            request.model = property.deployName;
        }
        Request.Builder builder = authorizationRequestBuilder(property.getAuth())
                .url(url)
                .post(RequestBody.create(JacksonUtils.serialize(request), MediaType.parse("application/json")));
        return builder.build();
    }
}
