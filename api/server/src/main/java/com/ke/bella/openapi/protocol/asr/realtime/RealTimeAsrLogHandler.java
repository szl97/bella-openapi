package com.ke.bella.openapi.protocol.asr.realtime;

import org.springframework.stereotype.Component;

import com.ke.bella.openapi.EndpointProcessData;
import com.ke.bella.openapi.protocol.log.EndpointLogHandler;

@Component
public class RealTimeAsrLogHandler implements EndpointLogHandler {
    @Override
    public void process(EndpointProcessData processData) {
        processData.setUsage(processData.getDuration());
    }

    @Override
    public String endpoint() {
        return "/v1/audio/asr/stream";
    }
}
