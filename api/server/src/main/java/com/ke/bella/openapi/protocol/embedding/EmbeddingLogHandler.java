package com.ke.bella.openapi.protocol.embedding;

import com.ke.bella.openapi.EndpointProcessData;
import com.ke.bella.openapi.protocol.completion.CompletionProperty;
import com.ke.bella.openapi.protocol.log.EndpointLogHandler;
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
        long startTime = processData.getRequestTime();
        Map<String, Object> map = new HashMap<>();
        map.put("ttlt", System.currentTimeMillis() - startTime);
        processData.setMetrics(map);
        EmbeddingRequest request = (EmbeddingRequest) processData.getRequest();
        String encodingType = JacksonUtils.deserialize(processData.getChannelInfo(), CompletionProperty.class).getEncodingType();
        EmbeddingResponse response = (EmbeddingResponse) processData.getResponse();
        if(response.getUsage() != null) {
            processData.setUsage(response.getUsage());
        } else {
            int inputToken = countTokenUsage(request, encodingType);
            EmbeddingResponse.TokenUsage tokenUsage = new EmbeddingResponse.TokenUsage();
            tokenUsage.setPrompt_tokens(inputToken);
            tokenUsage.setTotal_tokens(inputToken);
        }
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
