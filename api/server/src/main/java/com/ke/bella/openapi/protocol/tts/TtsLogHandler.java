package com.ke.bella.openapi.protocol.tts;

import com.ke.bella.openapi.EndpointProcessData;
import com.ke.bella.openapi.protocol.OpenapiResponse;
import com.ke.bella.openapi.protocol.tts.TtsRequest;
import com.ke.bella.openapi.protocol.log.EndpointLogHandler;
import com.ke.bella.openapi.utils.DateTimeUtils;
import com.ke.bella.openapi.utils.TokenCounter;
import com.knuddels.jtokkit.api.EncodingType;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class TtsLogHandler implements EndpointLogHandler {
    @Override
    public void process(EndpointProcessData processData) {
        TtsRequest request = (TtsRequest) processData.getRequest();
        long startTime = processData.getRequestTime();
        if(processData.getMetrics() == null) {
            int ttlt = (int) (DateTimeUtils.getCurrentSeconds() - startTime);
            Map<String, Object> map = new HashMap<>();
            map.put("ttlt", ttlt);
            processData.setMetrics(map);
            processData.setDuration(ttlt);
        }
        processData.setUsage(request.getInput().length());
    }

    @Override
    public String endpoint() {
        return "/v1/audio/speech";
    }
}
