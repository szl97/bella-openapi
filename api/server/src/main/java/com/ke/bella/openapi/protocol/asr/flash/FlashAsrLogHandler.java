package com.ke.bella.openapi.protocol.asr.flash;

import com.ke.bella.openapi.EndpointProcessData;
import com.ke.bella.openapi.protocol.log.EndpointLogHandler;
import com.ke.bella.openapi.utils.DateTimeUtils;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class FlashAsrLogHandler implements EndpointLogHandler {
    @Override
    public void process(EndpointProcessData processData) {
        if(processData.getResponse().getError() != null) {
            processData.setUsage(1);
        }
        int ttlt = (int) (DateTimeUtils.getCurrentSeconds() - processData.getRequestTime());
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("ttlt", ttlt);
        processData.setMetrics(metrics);
        processData.setDuration(ttlt);

    }

    @Override
    public String endpoint() {
        return "/v1/audio/asr/flash";
    }
}
