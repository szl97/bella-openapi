package com.ke.bella.openapi.protocol;

import com.ke.bella.openapi.protocol.completion.StreamCompletionResponse;
import lombok.AllArgsConstructor;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

public interface Callback<T> {
     void callback(T msg) throws Exception;

     @AllArgsConstructor
     class CompletionSseCallback implements Callback<StreamCompletionResponse> {
         private SseEmitter sseEmitter;
         @Override
         public void callback(StreamCompletionResponse msg) throws IOException {
             sseEmitter.send(msg);
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
