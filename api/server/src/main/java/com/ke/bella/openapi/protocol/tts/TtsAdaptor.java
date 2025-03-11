package com.ke.bella.openapi.protocol.tts;

import com.ke.bella.openapi.EndpointProcessData;
import com.ke.bella.openapi.protocol.IProtocolAdaptor;
import com.ke.bella.openapi.protocol.completion.Callbacks;
import com.ke.bella.openapi.protocol.log.EndpointLogger;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

public interface TtsAdaptor <T extends TtsProperty> extends IProtocolAdaptor {

    ResponseEntity<byte[]> tts(TtsRequest request, String url, T property);

    void streamTts(TtsRequest request, String url, T property, Callbacks.StreamTtsCallback callback);

    Callbacks.StreamTtsCallback buildCallback(TtsRequest request, SseEmitter sse, EndpointProcessData processData, EndpointLogger logger);

    @Override
    default String endpoint() {
        return "/v1/audio/speech";
    }
}
