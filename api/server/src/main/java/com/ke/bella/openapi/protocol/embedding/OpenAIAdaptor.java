package com.ke.bella.openapi.protocol.embedding;

import com.ke.bella.openapi.utils.HttpUtils;
import com.ke.bella.openapi.utils.JacksonUtils;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.Collections;
import java.util.List;

@Component("OpenAIEmbedding")
public class OpenAIAdaptor implements EmbeddingAdaptor<OpenAIProperty> {

    @Override
    public EmbeddingResponse embedding(EmbeddingRequest request, String url, OpenAIProperty property) {
        List<?> inputs;
        if(request.getInput() instanceof String) {
            inputs = Collections.singletonList((String) request.getInput());
        } else {
            inputs = (List<?>) request.getInput();
            Assert.isTrue(inputs.size() <= property.getBatchSize(),
                    "input 长度不能超过" + property.getBatchSize());
        }
        if(StringUtils.isNotEmpty(property.getApiVersion())) {
            url += property.getApiVersion();
        }
        request.setInput(inputs);
        request.setModel(property.getDeployName());
        Request.Builder builder = authorizationRequestBuilder(property.getAuth())
                .url(url)
                .post(RequestBody.create(JacksonUtils.serialize(request), MediaType.parse("application/json")));
        Request httpRequest = builder.build();
        return doRequest(httpRequest);
    }

    protected EmbeddingResponse doRequest(Request httpRequest) {
        return HttpUtils.httpRequest(httpRequest, EmbeddingResponse.class);
    }

    @Override
    public String getDescription() {
        return "openAI协议";
    }

    @Override
    public Class<?> getPropertyClass() {
        return OpenAIProperty.class;
    }
}
