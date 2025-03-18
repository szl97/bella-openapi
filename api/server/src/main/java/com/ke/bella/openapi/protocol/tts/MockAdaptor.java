package com.ke.bella.openapi.protocol.tts;

import java.io.OutputStream;

import org.springframework.stereotype.Component;

import com.ke.bella.openapi.EndpointProcessData;
import com.ke.bella.openapi.common.exception.BizParamCheckException;
import com.ke.bella.openapi.protocol.Callbacks;
import com.ke.bella.openapi.protocol.log.EndpointLogger;

import javax.servlet.AsyncContext;

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
    public byte[] tts(TtsRequest request, String url, TtsProperty property) {
        throw new BizParamCheckException("尚未支持tts mock");
    }

    @Override
    public void streamTts(TtsRequest request, String url, TtsProperty property, Callbacks.StreamTtsCallback callback) {
        throw new BizParamCheckException("尚未支持tts mock");
    }

    @Override
    public Callbacks.StreamTtsCallback buildCallback(TtsRequest request, OutputStream outputStream, AsyncContext context,
            EndpointProcessData processData, EndpointLogger logger) {
        throw new BizParamCheckException("尚未支持tts mock");
    }

}
