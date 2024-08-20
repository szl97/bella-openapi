package com.ke.bella.openapi.protocol.completion;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.ke.bella.openapi.protocol.AuthorizationProperty;
import com.ke.bella.openapi.protocol.IProtocolProperty;
import com.ke.bella.openapi.protocol.IProtocolAdaptor;
import com.ke.bella.openapi.protocol.OpenapiResponse;
import com.ke.bella.openapi.utils.HttpUtils;
import com.ke.bella.openapi.utils.JacksonUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("AliCompletion")
public class AliAdaptor implements IProtocolAdaptor.CompletionAdaptor<AliAdaptor.AliProperty> {
    @Override
    public Class<?> getPropertyClass() {
        return AliProperty.class;
    }

    @Override
    public CompletionResponse httpRequest(CompletionRequest request, String url, AliProperty property) {
        AliCompletionRequest aliRequest = requestConvert.callback(request);
        Request httpRequest = buildRequest(aliRequest, url, property);
        AliCompletionResponse response = HttpUtils.httpRequest(httpRequest, AliCompletionResponse.class);
        return responseConvert.callback(response);
    }

    @Override
    public void streamRequest(CompletionRequest request, String url, AliProperty property, Callback.CompletionSseCallback callback) {
        AliCompletionRequest aliRequest = requestConvert.callback(request);
        Request httpRequest = buildRequest(aliRequest, url, property);
        HttpUtils.streamRequest(httpRequest, new CompletionSseListener(callback, sseConverter));
    }

    private Request buildRequest(AliCompletionRequest request, String url, AliAdaptor.AliProperty property) {
        request.setModel(property.getDeployName());
        Request.Builder builder = authorizationRequestBuilder(property.getAuth())
                .url(url)
                .post(RequestBody.create(MediaType.parse("application/json"),
                        JSON.toJSONString(request)));
        return builder.build();
    }

    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AliProperty implements IProtocolProperty {
        AuthorizationProperty auth;
        String deployName;
    }

    private CompletionResponse.TokenUsage convertTokenUsage(AliCompletionResponse.AliUsage aliUsage) {
        if(aliUsage == null) {
            return null;
        }
        CompletionResponse.TokenUsage usage = new CompletionResponse.TokenUsage();
        usage.setPrompt_tokens(aliUsage.getInputTokens() + aliUsage.getAudioTokens() + aliUsage.getImageTokens());
        usage.setCompletion_tokens(aliUsage.getOutTokens());
        usage.setTotal_tokens(usage.getPrompt_tokens() + usage.getCompletion_tokens());
        return usage;
    }

    private final Callback.SseConvertCallback<String> sseConverter = str -> {
        AliCompletionResponse response = JacksonUtils.deserialize(str, AliCompletionResponse.class);
        OpenapiResponse.OpenapiError openAIError = null;
        List<StreamCompletionResponse.Choice> choices = null;
        if(response.getOutput() == null) {
            openAIError = OpenapiResponse.OpenapiError.builder()
                    .type(response.getCode())
                    .message(response.getMessage())
                    .build();
        } else {
            choices = Lists.newArrayList(StreamCompletionResponse.Choice.builder()
                    .delta(response.getOutput().getChoices().get(0).getMessage())
                    .finish_reason(response.getOutput().getChoices().get(0).getFinish_reason())
                    .build());
        }
        return StreamCompletionResponse.builder()
                .choices(choices)
                .usage(convertTokenUsage(response.getUsage()))
                .error(openAIError)
                .build();
    };

    private final Callback.ConvertCallback<AliCompletionResponse> responseConvert = response -> {
        if(response == null) {
            return null;
        }
        OpenapiResponse.OpenapiError openAIError = null;
        List<CompletionResponse.Choice> choices = null;
        if(response.getOutput() == null) {
            openAIError = OpenapiResponse.OpenapiError.builder()
                    .type(response.getCode())
                    .message(response.getMessage())
                    .build();
        } else {
            choices = response.getOutput().getChoices();
        }
        return CompletionResponse.builder()
                .id(response.getRequestId())
                .choices(choices)
                .usage(convertTokenUsage(response.getUsage()))
                .error(openAIError)
                .build();
    };

    private final Callback.RequestConvertCallback<AliCompletionRequest> requestConvert = request -> {
        AliCompletionRequest.AliCompletionInput input = AliCompletionRequest.AliCompletionInput.builder()
                .messages(request.getMessages())
                .build();
        AliCompletionRequest.AliCompletionParameters parameters = AliCompletionRequest.AliCompletionParameters.builder()
                .topP(request.getTop_p())
                .temperature(request.getTemperature())
                .seed(request.getSeed())
                .maxTokens(request.getMax_tokens())
                .stopString(request.getStop())
                .tools(request.getTools())
                .incremental_output(request.isStream())
                .build();
        return AliCompletionRequest.builder()
                .model(request.getModel())
                .input(input)
                .parameters(parameters).build();
    };
}
