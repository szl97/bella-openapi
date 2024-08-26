package com.ke.bella.openapi.protocol.completion;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.ke.bella.openapi.utils.SseHelper;

import lombok.AllArgsConstructor;

public interface Callbacks {

     @FunctionalInterface
     interface SseEventConverter<T> {
         T convert(String id, String event, String msg);
     }

     @AllArgsConstructor
     class StreamCompletionCallback {
         private SseEmitter sse;

         public void callback(StreamCompletionResponse msg) {
             SseHelper.sendEvent(sse, msg);
         }

         public void done() {
             SseHelper.sendEvent(sse, "[DONE]");
         }

         public void finish() {
             sse.complete();
         }

         public void finishWithException(Throwable t) {
             sse.completeWithError(t);
         }
     }
}
