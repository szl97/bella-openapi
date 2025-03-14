package com.ke.bella.openapi.protocol.completion;

import com.ke.bella.openapi.EndpointProcessData;
import com.ke.bella.openapi.protocol.Callbacks;

public class QueueAdaptor<T extends CompletionProperty> implements CompletionAdaptor<T> {
    private final CompletionAdaptorDelegator<T> delegator;
    private final Callbacks.RealTimeTaskCallback realTimeTaskCallback;
    private final Callbacks.StreamTaskCallback streamTaskCallback;
    private final EndpointProcessData processData;

    public QueueAdaptor(CompletionAdaptorDelegator<T> delegator, Callbacks.RealTimeTaskCallback realTimeTaskCallback,
            Callbacks.StreamTaskCallback streamTaskCallback, EndpointProcessData processData) {
        this.delegator = delegator;
        this.realTimeTaskCallback = realTimeTaskCallback;
        this.streamTaskCallback = streamTaskCallback;
        this.processData = processData;
    }

    Callbacks.HttpDelegator httpDelegator(T property) {
       return new Callbacks.HttpDelegator() {
            @Override
            public <T> T request(Object req, Class<T> clazz, Callbacks.ChannelErrorCallback<T> errorCallback) {
                return realTimeTaskCallback.putRealTimeTask(req, processData.getEndpoint(), property.getQueueName(),
                        processData.getApikey(), getTimeout(false), clazz, errorCallback);
            }
        };
    }

    Callbacks.StreamDelegator streamDelegator(T property) {
        return (req, listener) -> streamTaskCallback.putStreamTask(req, processData.getEndpoint(), property.getQueueName(),
                processData.getApikey(), getTimeout(true), listener);
    }

    @Override
    public CompletionResponse completion(CompletionRequest request, String url, T property) {
       return delegator.completion(request, url, property, httpDelegator(property));
    }

    @Override
    public void streamCompletion(CompletionRequest request, String url, T property, Callbacks.StreamCompletionCallback callback) {
        delegator.streamCompletion(request, url, property, callback, streamDelegator(property));
    }

    @Override
    public String getDescription() {
        return "jobQueue协议";
    }

    @Override
    public Class<?> getPropertyClass() {
        return delegator.getPropertyClass();
    }

    private int getTimeout(boolean stream) {
        return processData.getMaxWaitSec() != null ? processData.getMaxWaitSec() :
                (stream ? streamTaskCallback.defaultTimeout() : realTimeTaskCallback.defaultTimeout());
    }
}
