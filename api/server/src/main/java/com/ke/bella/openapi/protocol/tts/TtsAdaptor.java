package com.ke.bella.openapi.protocol.tts;

import com.ke.bella.openapi.protocol.IProtocolAdaptor;
import org.springframework.http.ResponseEntity;

import java.io.IOException;

public interface TtsAdaptor <T extends TtsProperty> extends IProtocolAdaptor {

    ResponseEntity<byte[]> tts(TtsRequest request, String url, T property);

    @Override
    default String endpoint() {
        return "/v1/audio/speech";
    }
}
