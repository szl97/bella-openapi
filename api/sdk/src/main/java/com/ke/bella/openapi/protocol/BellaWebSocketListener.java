package com.ke.bella.openapi.protocol;

import lombok.Setter;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

@Setter
public class BellaWebSocketListener extends WebSocketListener {
    protected CompletableFuture<?> connectionInitFuture;

    @Override
    public void onOpen(WebSocket webSocket, Response response) {
        connectionInitFuture.complete(null);
    }
}
