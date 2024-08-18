package com.ke.bella.openapi.protocol.completion;

import com.google.common.collect.ImmutableSet;

import lombok.Setter;
import okhttp3.Response;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class CompletionSseListener extends EventSourceListener {
    @Setter
    private CompletableFuture<?> connectionInitFuture;
    private Callback.CompletionSseCallback callback;
    private SseConverter sseConverter;
    private final Set<String> DONE_FLAGS = ImmutableSet.of("[DONE]");

    public CompletionSseListener(Callback.CompletionSseCallback sseCallback,
                                SseConverter sseConverter) {
        this.callback = sseCallback;
        this.sseConverter = sseConverter;
    }

    @Override
    public void onOpen(EventSource eventSource, Response response) {
        this.connectionInitFuture.complete(null);
    }

    @Override
    public void onEvent(EventSource eventSource, String id, String type, String msg) {
        try {
            if(DONE_FLAGS.contains(msg)) {
                callback.done();
            } else {
                StreamCompletionResponse response = sseConverter.convert(msg);
                if(response != null) {
                    callback.callback(response);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onClosed(EventSource eventSource) {
        callback.finish();
    }

    @Override
    public void onFailure(EventSource eventSource, Throwable t, Response response) {
        callback.finishWithException(t);
    }

    @FunctionalInterface
    interface SseConverter {
        StreamCompletionResponse convert(String str);
    }
}
