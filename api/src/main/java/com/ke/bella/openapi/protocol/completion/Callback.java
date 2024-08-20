package com.ke.bella.openapi.protocol.completion;

import lombok.AllArgsConstructor;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

public interface Callback<T, R> {
     R callback(T msg);

     @FunctionalInterface
     interface SseConvertCallback extends Callback<String, StreamCompletionResponse> {
     }

    @FunctionalInterface
    interface ConvertCallback<T> extends Callback<T, CompletionResponse> {
    }

    @FunctionalInterface
    interface RequestConvertCallback<R> extends Callback<CompletionRequest, R> {
    }

     @AllArgsConstructor
     class CompletionSseCallback implements Callback<StreamCompletionResponse, Boolean> {
         private SseEmitter sseEmitter;
         @Override
         public Boolean callback(StreamCompletionResponse msg) {
             try {
                 sseEmitter.send(msg);
                 return true;
             } catch (IOException e) {
                 throw new RuntimeException(e);
             }
         }

         public void done() throws IOException {
             sseEmitter.send("[DONE]");
         }

         public void finish() {
             sseEmitter.complete();
         }

         public void finishWithException(Throwable t) {
             sseEmitter.completeWithError(t);
         }
     }
}
