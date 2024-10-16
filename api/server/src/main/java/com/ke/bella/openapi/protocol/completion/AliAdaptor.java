package com.ke.bella.openapi.protocol.completion;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.ke.bella.openapi.protocol.OpenapiResponse;
import com.ke.bella.openapi.protocol.completion.Callbacks.StreamCompletionCallback;
import com.ke.bella.openapi.utils.DateTimeUtils;
import com.ke.bella.openapi.utils.HttpUtils;
import com.ke.bella.openapi.utils.JacksonUtils;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("AliCompletion")
public class AliAdaptor implements CompletionAdaptor<AliProperty> {
    @Override
    public Class<?> getPropertyClass() {
        return AliProperty.class;
    }

    @Override
    public CompletionResponse completion(CompletionRequest request, String url, AliProperty property) {
        AliCompletionRequest aliRequest = requestConvert(request);
        Request httpRequest = buildRequest(aliRequest, url, property);
        AliCompletionResponse response = HttpUtils.httpRequest(httpRequest, AliCompletionResponse.class);
        return responseConvert(response);
    }

    @Override
    public void streamCompletion(CompletionRequest request, String url, AliProperty property, StreamCompletionCallback callback) {
        AliCompletionRequest aliRequest = requestConvert(request);
        Request httpRequest = buildRequest(aliRequest, url, property);
        HttpUtils.streamRequest(httpRequest, new CompletionSseListener(callback, sseConverter));
    }

    private Request buildRequest(AliCompletionRequest request, String url, AliProperty property) {
        request.setModel(property.getDeployName());
        Request.Builder builder = authorizationRequestBuilder(property.getAuth())
                .url(url)
                .post(RequestBody.create(JSON.toJSONString(request),
                        MediaType.parse("application/json")));
        return builder.build();
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

    private final Callbacks.SseEventConverter<StreamCompletionResponse> sseConverter = (id, event, msg) -> {
        AliCompletionResponse response = JacksonUtils.deserialize(msg, AliCompletionResponse.class);
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
                .created(DateTimeUtils.getCurrentSeconds())
                .choices(choices)
                .usage(convertTokenUsage(response.getUsage()))
                .error(openAIError)
                .build();
    };

    private CompletionResponse responseConvert(AliCompletionResponse response) {
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
                .created(DateTimeUtils.getCurrentSeconds())
                .usage(convertTokenUsage(response.getUsage()))
                .error(openAIError)
                .build();
    }

    private AliCompletionRequest requestConvert(CompletionRequest request) {
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
    }
}
