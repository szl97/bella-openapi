package com.ke.bella.openapi.protocol.asr.flash;

import com.ke.bella.openapi.EndpointProcessData;
import com.ke.bella.openapi.protocol.log.EndpointLogHandler;
import org.springframework.stereotype.Component;

@Component
public class FlashAsrLogHandler implements EndpointLogHandler {
    @Override
    public void process(EndpointProcessData processData) {
        processData.setUsage(1);
    }

    @Override
    public String endpoint() {
        return "/v1/audio/asr/flash";
    }
}
