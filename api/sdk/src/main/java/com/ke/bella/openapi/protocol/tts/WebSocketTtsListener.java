package com.ke.bella.openapi.protocol.tts;

import com.ke.bella.openapi.protocol.BellaWebSocketListener;

import com.ke.bella.openapi.protocol.Callbacks;
import okhttp3.Response;
import okhttp3.WebSocket;
import okio.ByteString;

public class WebSocketTtsListener extends BellaWebSocketListener {
    private final Callbacks.WebSocketTtsCallback callback;

    public WebSocketTtsListener(Callbacks.WebSocketTtsCallback callback) {
        this.callback = callback;
    }

    @Override
    public void onOpen(WebSocket webSocket, Response response) {
        super.onOpen(webSocket, response);
        callback.onOpen(webSocket, response);
    }

    @Override
    public void onMessage(WebSocket webSocket, ByteString bytes) {
        callback.onMessage(webSocket, bytes);
    }


    @Override
    public void onMessage(WebSocket webSocket, String text) {
    }

    @Override
    public void onClosed(WebSocket webSocket, int code, String reason) {
        callback.onClosed(webSocket, code, reason);
    }

    @Override
    public void onClosing(WebSocket webSocket, int code, String reason) {
        callback.onClosing(webSocket, code, reason);
    }

    @Override
    public void onFailure(WebSocket webSocket, Throwable t, Response response) {
        callback.onFailure(webSocket, t, response);
    }
}
