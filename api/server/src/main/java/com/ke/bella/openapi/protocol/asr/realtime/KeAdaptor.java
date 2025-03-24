package com.ke.bella.openapi.protocol.asr.realtime;

import org.springframework.stereotype.Component;

import com.ke.bella.openapi.EndpointProcessData;
import com.ke.bella.openapi.protocol.BellaWebSocketListener;
import com.ke.bella.openapi.protocol.Callbacks;
import com.ke.bella.openapi.protocol.asr.AsrProperty;
import com.ke.bella.openapi.protocol.asr.KeStreamAsrCallback;
import com.ke.bella.openapi.protocol.log.EndpointLogger;
import com.ke.bella.openapi.utils.HttpUtils;
import com.ke.bella.openapi.utils.JacksonUtils;

import okhttp3.Request;
import okhttp3.WebSocket;
import okio.ByteString;

@Component("KeRealtimeAsr")
public class KeAdaptor implements RealTimeAsrAdaptor<AsrProperty> {

    @Override
    public WebSocket startTranscription(String url, AsrProperty property, RealTimeAsrMessage request, Callbacks.WebSocketCallback callback) {
        Request.Builder builder = new Request.Builder()
                .url(url);
        WebSocket webSocket = HttpUtils.websocketRequest(builder.build(), new BellaWebSocketListener(callback));
        webSocket.send(JacksonUtils.serialize(request));
        callback.started();
        return webSocket;
    }

    @Override
    public boolean sendAudioData(WebSocket webSocket, byte[] audioData, Callbacks.WebSocketCallback callback) {
        return webSocket.send(ByteString.of(audioData));
    }

    @Override
    public boolean stopTranscription(WebSocket webSocket, RealTimeAsrMessage request, Callbacks.WebSocketCallback callback) {
        return webSocket.send(JacksonUtils.serialize(request));
    }

    @Override
    public void closeConnection(WebSocket webSocket) {
        webSocket.close(1000, "client close");
    }

    @Override
    public Callbacks.WebSocketCallback createCallback(Callbacks.TextSender sender, EndpointProcessData processData, EndpointLogger logger,
            String taskId, RealTimeAsrMessage request, AsrProperty property) {
        return new KeStreamAsrCallback(sender, processData, logger, taskId);
    }

    @Override
    public String getDescription() {
        return "贝壳协议";
    }

    @Override
    public Class<AsrProperty> getPropertyClass() {
        return AsrProperty.class;
    }
}
