package com.ke.bella.openapi.protocol.asr.realtime;

import com.ke.bella.openapi.EndpointProcessData;
import com.ke.bella.openapi.protocol.Callbacks;
import com.ke.bella.openapi.protocol.IProtocolAdaptor;
import com.ke.bella.openapi.protocol.asr.AsrProperty;
import com.ke.bella.openapi.protocol.Callbacks.WebSocketCallback;
import com.ke.bella.openapi.protocol.log.EndpointLogger;
import okhttp3.WebSocket;

public interface RealTimeAsrAdaptor<T extends AsrProperty> extends IProtocolAdaptor {

    @Override
    default String endpoint() {
        return "/v1/audio/asr/stream";
    }

    WebSocket startTranscription(String url, T property, RealTimeAsrMessage request, WebSocketCallback callback);

    boolean sendAudioData(WebSocket webSocket, byte[] audioData, WebSocketCallback callback);

    boolean stopTranscription(WebSocket webSocket, RealTimeAsrMessage request, WebSocketCallback callback);

    void closeConnection(WebSocket webSocket);

    WebSocketCallback createCallback(Callbacks.TextSender sender, EndpointProcessData processData, EndpointLogger logger, String taskId,
            RealTimeAsrMessage request, T property);

    Class<T> getPropertyClass();
}
