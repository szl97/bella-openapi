package com.ke.bella.openapi.protocol.embedding;

import com.ke.bella.openapi.EndpointProcessData;
import com.ke.bella.openapi.protocol.OpenapiResponse;
import com.ke.bella.openapi.protocol.completion.CompletionProperty;
import com.ke.bella.openapi.protocol.log.EndpointLogHandler;
import com.ke.bella.openapi.utils.DateTimeUtils;
import com.ke.bella.openapi.utils.JacksonUtils;
import com.ke.bella.openapi.utils.TokenCounter;
import com.knuddels.jtokkit.api.EncodingType;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class EmbeddingLogHandler implements EndpointLogHandler {
    @Override
    public void process(EndpointProcessData processData) {
        EmbeddingRequest request = (EmbeddingRequest) processData.getRequest();
        String encodingType = JacksonUtils.deserialize(processData.getChannelInfo(), CompletionProperty.class).getEncodingType();
        EmbeddingResponse response = null;
        if(processData.getResponse() instanceof EmbeddingResponse) {
            response = (EmbeddingResponse) processData.getResponse();
        }
        EmbeddingResponse.TokenUsage usage;
        if(response != null && response.getUsage() != null) {
            usage = response.getUsage();
        } else {
            int inputToken = 0;
            OpenapiResponse.OpenapiError error = processData.getResponse().getError();
            if(error == null || (error.getCode() > 399 && error.getCode() < 500 && error.getCode() != 408)) {
                inputToken = countTokenUsage(request, encodingType);
            }
            usage = new EmbeddingResponse.TokenUsage();
            usage.setPrompt_tokens(inputToken);
            usage.setTotal_tokens(inputToken);
        }
        long startTime = processData.getRequestTime();
        int ttlt = (int) (DateTimeUtils.getCurrentSeconds() - startTime);
        Map<String, Object> map = new HashMap<>();
        map.put("ttlt", ttlt);
        map.put("token", usage.getTotal_tokens());
        processData.setMetrics(map);
        processData.setUsage(usage);
    }

    private Integer countTokenUsage(EmbeddingRequest request, String encodingType) {
        EncodingType encoding = EncodingType.fromName(encodingType).orElse(EncodingType.CL100K_BASE);
        if(request.getInput() instanceof String) {
            return TokenCounter.tokenCount((String) request.getInput(), encoding);
        } else {
            List<String> inputList = (List<String>) request.getInput();
            return inputList.stream().map(x -> TokenCounter.tokenCount(x, encoding)).reduce(Integer::sum).orElse(0);
        }
    }

    @Override
    public String endpoint() {
        return null;
    }
}
