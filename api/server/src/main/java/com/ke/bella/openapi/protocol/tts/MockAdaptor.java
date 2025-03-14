package com.ke.bella.openapi.protocol.tts;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.ke.bella.openapi.EndpointProcessData;
import com.ke.bella.openapi.common.exception.BizParamCheckException;
import com.ke.bella.openapi.protocol.Callbacks;
import com.ke.bella.openapi.protocol.log.EndpointLogger;

@Component("mockTts")
public class MockAdaptor implements TtsAdaptor<TtsProperty> {
    @Override
    public String getDescription() {
        return "mock协议";
    }

    @Override
    public Class<?> getPropertyClass() {
        return TtsAdaptor.class;
    }

    @Override
    public ResponseEntity<byte[]> tts(TtsRequest request, String url, TtsProperty property) {
        throw new BizParamCheckException("尚未支持tts mock");
    }

    @Override
    public void streamTts(TtsRequest request, String url, TtsProperty property, Callbacks.StreamTtsCallback callback) {
        throw new BizParamCheckException("尚未支持tts mock");
    }

    @Override
    public Callbacks.StreamTtsCallback buildCallback(TtsRequest request, SseEmitter sse, EndpointProcessData processData, EndpointLogger logger) {
        throw new BizParamCheckException("尚未支持tts mock");
    }

}
