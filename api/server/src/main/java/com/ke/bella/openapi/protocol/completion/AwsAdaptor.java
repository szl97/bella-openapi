package com.ke.bella.openapi.protocol.completion;

import com.ke.bella.openapi.exception.ChannelException;
import com.ke.bella.openapi.protocol.completion.Callbacks.StreamCompletionCallback;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeAsyncClient;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.bedrockruntime.model.ContentBlockDeltaEvent;
import software.amazon.awssdk.services.bedrockruntime.model.ContentBlockStartEvent;
import software.amazon.awssdk.services.bedrockruntime.model.ConverseRequest;
import software.amazon.awssdk.services.bedrockruntime.model.ConverseResponse;
import software.amazon.awssdk.services.bedrockruntime.model.ConverseStreamMetadataEvent;
import software.amazon.awssdk.services.bedrockruntime.model.ConverseStreamRequest;
import software.amazon.awssdk.services.bedrockruntime.model.ConverseStreamResponseHandler;

import java.util.function.Consumer;

@Component("AwsCompletion")
public class AwsAdaptor implements CompletionAdaptor<AwsProperty> {

    @Override
    public Class<?> getPropertyClass() {
        return AwsProperty.class;
    }

    @Override
    public CompletionResponse completion(CompletionRequest request, String url, AwsProperty property) {
        request.setModel(property.deployName);
        ConverseRequest awsRequest = AwsCompletionConverter.convert2AwsRequest(request);
        BedrockRuntimeClient client = AwsClientManager.client(property.region, property.auth.getApiKey(), property.auth.getSecret());
        ConverseResponse response = client.converse(awsRequest);
        return AwsCompletionConverter.convert2OpenAIResponse(response);
    }

    @Override
    public void streamCompletion(CompletionRequest request, String url, AwsProperty property,
            StreamCompletionCallback callback) {
        request.setModel(property.deployName);
        ConverseStreamRequest awsRequest = AwsCompletionConverter.convert2AwsStreamRequest(request);
        BedrockRuntimeAsyncClient client = AwsClientManager.asyncClient(property.region, property.auth.getApiKey(), property.auth.getSecret());
        AwsSseCompletionCallBack awsCallBack = new AwsSseCompletionCallBack(callback);
        client.converseStream(awsRequest, ConverseStreamResponseHandler.builder()
                .subscriber(awsCallBack)
                .onError(awsCallBack)
                .onComplete(awsCallBack)
                .build());
    }

    @AllArgsConstructor
    static class AwsSseCompletionCallBack implements ConverseStreamResponseHandler.Visitor, Consumer<Throwable>, Runnable {
        private Callbacks.StreamCompletionCallback callback;

        @Override
        public void visitContentBlockStart(ContentBlockStartEvent event) {
            StreamCompletionResponse response = AwsCompletionConverter.convert2OpenAIStreamResponse(event.start(), event.contentBlockIndex());
            callback.callback(response);
        }

        @Override
        public void visitContentBlockDelta(ContentBlockDeltaEvent event) {
            StreamCompletionResponse response = AwsCompletionConverter.convert2OpenAIStreamResponse(event.delta(), event.contentBlockIndex());
            callback.callback(response);
        }

        @Override
        public void visitMetadata(ConverseStreamMetadataEvent event) {
            StreamCompletionResponse response = AwsCompletionConverter.convertTo2OpenAIStreamResponse(event.usage());
            callback.callback(response);
            callback.done();
        }

        @Override
        public void run() {
            callback.finish();
        }

        @Override
        public void accept(Throwable throwable) {
            callback.finish(ChannelException.fromException(throwable));
        }
    }
}
