package com.ke.bella.openapi.protocol.completion;

import com.ke.bella.openapi.protocol.AuthorizationProperty;
import com.ke.bella.openapi.protocol.IProtocolAdaptor;
import com.ke.bella.openapi.protocol.IProtocolProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
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
public class AwsAdaptor implements IProtocolAdaptor.CompletionAdaptor<AwsAdaptor.AwsProperty> {

    @Override
    public Class<?> getPropertyClass() {
        return AwsProperty.class;
    }

    @Override
    public CompletionResponse httpRequest(CompletionRequest request, String url, AwsAdaptor.AwsProperty property) {
        request.setModel(property.deployName);
        ConverseRequest awsRequest = requestConvert.callback(request);
        BedrockRuntimeClient client = AwsClientManager.client(property.region, property.auth.getApiKey(), property.auth.getSecret());
        ConverseResponse response = client.converse(awsRequest);
        return responseConvert.callback(response);
    }

    @Override
    public void streamRequest(CompletionRequest request, String url, AwsAdaptor.AwsProperty property, Callback.CompletionSseCallback callback) {
        request.setModel(property.deployName);
        ConverseStreamRequest awsRequest = streamRequestConvert.callback(request);
        BedrockRuntimeAsyncClient client = AwsClientManager.asyncClient(property.region, property.auth.getApiKey(), property.auth.getSecret());
        AwsSseCompletionCallBack awsCallBack = new AwsSseCompletionCallBack(callback);
        client.converseStream(awsRequest, ConverseStreamResponseHandler.builder()
                        .subscriber(awsCallBack)
                        .onError(awsCallBack)
                        .onComplete(awsCallBack)
                        .build());
    }

    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AwsProperty implements IProtocolProperty {
        AuthorizationProperty auth;
        String region;
        String deployName;
    }

    private final Callback.ConvertCallback<ConverseResponse> responseConvert = AwsCompletionConverter::convert2OpenAIResponse;

    private final Callback.RequestConvertCallback<ConverseRequest> requestConvert = AwsCompletionConverter::convert2AwsRequest;

    private final Callback.RequestConvertCallback<ConverseStreamRequest> streamRequestConvert = AwsCompletionConverter::convert2AwsStreamRequest;

    @AllArgsConstructor
    static class AwsSseCompletionCallBack implements ConverseStreamResponseHandler.Visitor, Consumer<Throwable>, Runnable {
        private Callback.CompletionSseCallback callback;
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
            callback.finishWithException(throwable);
        }
    }
}
