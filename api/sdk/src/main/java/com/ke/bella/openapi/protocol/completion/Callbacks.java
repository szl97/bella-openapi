package com.ke.bella.openapi.protocol.completion;

import okhttp3.WebSocket;
import okio.ByteString;
import org.springframework.util.Assert;

import com.ke.bella.openapi.common.exception.ChannelException;
import com.ke.bella.openapi.utils.DateTimeUtils;
import com.ke.bella.openapi.utils.JacksonUtils;

import okhttp3.Response;

public interface Callbacks {

    @FunctionalInterface
    interface SseEventConverter<T> {
        T convert(String id, String event, String msg);
    }

    class DefaultSseConverter implements SseEventConverter<StreamCompletionResponse> {

        @Override
        public StreamCompletionResponse convert(String id, String event, String msg) {
            StreamCompletionResponse response = JacksonUtils.deserialize(msg, StreamCompletionResponse.class);
            if(response != null) {
                response.setCreated(DateTimeUtils.getCurrentSeconds());
            }
            return response;
        }
    }

    interface StreamCompletionCallback extends Callbacks {

        void onOpen();

        void callback(StreamCompletionResponse msg);

        void done();

        void finish();

        void finish(ChannelException exception);

        default boolean support() {
            return true;
        }
    }

    interface StreamTtsCallback extends Callbacks {
    }

    interface WebSocketTtsCallback extends StreamTtsCallback {
       void onOpen(WebSocket webSocket, Response response);
       void onMessage(WebSocket webSocket, ByteString bytes);
       void onMessage(WebSocket webSocket, String text);
       void onClosing(WebSocket webSocket, int code, String reason);
       void onClosed(WebSocket webSocket, int code, String reason);
       void onFailure(WebSocket webSocket, Throwable t, Response response);
    }

    interface ChannelErrorCallback<T> {
        void callback(T channelResponse, Response httpResponse);
    }

    abstract class StreamCompletionCallbackNode implements StreamCompletionCallback {
        protected StreamCompletionCallback next;

        public StreamCompletionCallbackNode() {

        }

        @Override
        public void onOpen() {
            next.onOpen();
        }

        @Override
        public void callback(StreamCompletionResponse msg) {
            try {
                if(support()) {
                    msg = doCallback(msg);
                }
            } catch (Exception e) {
                finish(ChannelException.fromException(e));
                e.printStackTrace();
            } finally {
                if(next != null) {
                    next.callback(msg);
                }
            }
        }

        public abstract StreamCompletionResponse doCallback(StreamCompletionResponse msg);

        @Override
        public void done() {
            if(next != null) {
                next.done();
            }
        }

        @Override
        public void finish() {
            if(next != null) {
                next.finish();
            }
        }

        @Override
        public void finish(ChannelException exception) {
            if(next != null) {
                next.finish(exception);
            }
        }

        public void addLast(StreamCompletionCallback callback) {
            if(next == null) {
                next = callback;
            } else {
                Assert.isTrue(next instanceof StreamCompletionCallbackNode, "callback chain must be StreamCompletionCallbackNode");
                ((StreamCompletionCallbackNode) next).addLast(callback);
            }
        }
    }
}
