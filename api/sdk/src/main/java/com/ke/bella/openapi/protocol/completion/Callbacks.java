package com.ke.bella.openapi.protocol.completion;

import com.ke.bella.openapi.common.exception.ChannelException;
import okhttp3.Response;
import org.springframework.util.Assert;

public interface Callbacks {

    @FunctionalInterface
    interface SseEventConverter<T> {
        T convert(String id, String event, String msg);
    }

    interface StreamCompletionCallback extends Callbacks {
        void callback(StreamCompletionResponse msg);

        void done();

        void finish();

        void finish(ChannelException exception);

        default boolean support() {
            return true;
        }
    }

    interface ChannelErrorCallback<T> {
        void callback(T channelResponse, Response httpResponse);
    }

    abstract class StreamCompletionCallbackNode implements StreamCompletionCallback {
        protected StreamCompletionCallback next;

        public StreamCompletionCallbackNode() {

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
