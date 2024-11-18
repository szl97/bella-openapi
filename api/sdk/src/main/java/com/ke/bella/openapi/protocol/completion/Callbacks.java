package com.ke.bella.openapi.protocol.completion;

import com.ke.bella.openapi.common.exception.ChannelException;

public interface Callbacks {

     @FunctionalInterface
     interface SseEventConverter<T> {
         T convert(String id, String event, String msg);
     }
     interface StreamCompletionCallback {
         void callback(StreamCompletionResponse msg);

         void done();

         void finish();

         void finish(ChannelException exception);
     }
}
