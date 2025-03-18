package com.ke.bella.openapi.protocol;

import java.util.concurrent.CompletableFuture;

import lombok.Setter;
import okhttp3.Callback;

@Setter
public abstract class BellaStreamCallback implements Callback {
    protected CompletableFuture<?> connectionInitFuture;
    protected void onOpen() {
        this.connectionInitFuture.complete(null);
    }
}
