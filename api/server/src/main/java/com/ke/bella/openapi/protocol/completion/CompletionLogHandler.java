package com.ke.bella.openapi.protocol.completion;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.ke.bella.openapi.EndpointProcessData;
import com.ke.bella.openapi.protocol.log.EndpointLogHandler;
import com.ke.bella.openapi.utils.JacksonUtils;
import com.ke.bella.openapi.utils.TokenCounter;
import com.knuddels.jtokkit.api.EncodingType;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class CompletionLogHandler implements EndpointLogHandler {

    @Override
    public void process(EndpointProcessData processData) {
        long startTime = processData.getRequestTime();
        CompletionResponse response = (CompletionResponse) processData.getResponse();
        long firstPackageTime = processData.getFirstPackageTime() == 0L ? response.getCreated()
                : processData.getFirstPackageTime();
        processData.setMetrics(countMetrics(startTime, response.getCreated(), firstPackageTime));
        CompletionRequest request = (CompletionRequest) processData.getRequest();
        String encodingType = JacksonUtils.deserialize(processData.getChannelInfo(), CompletionProperty.class).getEncodingType();
        processData.setUsage(countTokenUsage(request, response, encodingType));
    }

    @Override
    public String endpoint() {
        return "/v1/chat/completions";
    }

    private Map<String, Object> countMetrics(long startTime, long endTime, long firstPackageTime) {
        return ImmutableMap.of("ttft", firstPackageTime - startTime, "ttlt", endTime - startTime);
    }

    private CompletionResponse.TokenUsage countTokenUsage(CompletionRequest request, CompletionResponse response, String encodingType) {
        if(response.getUsage() != null) {
            return response.getUsage();
        }
        EncodingType encoding = EncodingType.fromName(encodingType).orElse(EncodingType.CL100K_BASE);
        //计费模型请求消耗量
        //计算非userMessage的token用量
        int requestToken = 0;
        List<String> textMessage = new LinkedList<>();
        List<Pair<String, Boolean>> imgMessage = new LinkedList<>();
        if(request.getMessages() != null) {
            for (Message message : request.getMessages()) {
                if(CollectionUtils.isNotEmpty(message.getTool_calls())) {
                    textMessage.addAll(getToolCallStr(message.getTool_calls()));
                } else {
                    //如果message.getContent()是String类型
                    if(message.getContent() instanceof String) {
                        textMessage.add((String) message.getContent());
                    } else if(message.getContent() instanceof java.util.List) {
                        for (Map content : (java.util.List<Map>) message.getContent()) {
                            if(content.containsKey("text")) {
                                textMessage.add((String) content.get("text"));
                            } else if(content.containsKey("image_url")) {
                                //如果包含类型为string的image_url
                                if(content.get("image_url") instanceof String) {
                                    imgMessage.add(Pair.of((String) content.get("image_url"), false));
                                } else if(content.get("image_url") instanceof Map) {
                                    String url = (String) ((Map) content.get("image_url")).get("url");
                                    boolean lowResolution = "low".equals(((Map) content.get("image_url")).get("detail"));
                                    imgMessage.add(Pair.of(url, lowResolution));
                                }
                            }
                        }
                    }
                }
            }
        }
        Optional<Integer> userTextMessageToken = textMessage.stream().map(x -> TokenCounter.tokenCount(x, encoding)).reduce(Integer::sum);
        Optional<Integer> userImgMessageToken = imgMessage.stream().map(x-> TokenCounter.imageToken(x.getLeft(), x.getRight())).reduce(Integer::sum);
        requestToken += userTextMessageToken.orElse(0) + userImgMessageToken.orElse(0);

        int responseToken = (response.getChoices() == null) ? 0 : response.getChoices().stream()
                .map(x -> {
                    if(CollectionUtils.isNotEmpty(x.getMessage().getTool_calls())) {
                        return getToolCallStr(x.getMessage().getTool_calls());
                    } else {
                        return Lists.newArrayList(x.getMessage().getContent());
                    }
                }).flatMap(List::stream)
                .map(String.class::cast)
                .map(x -> TokenCounter.tokenCount(x, encoding)).reduce(Integer::sum).orElse(0);
        CompletionResponse.TokenUsage tokenUsage = new CompletionResponse.TokenUsage();
        tokenUsage.setPrompt_tokens(requestToken);
        tokenUsage.setCompletion_tokens(responseToken);
        tokenUsage.setTotal_tokens(requestToken + responseToken);
        return tokenUsage;
    }

    private List<String> getToolCallStr(List<Message.ToolCall> toolCalls) {
        return toolCalls.stream()
                .map(t->getFunctionStr(t.getFunction()))
                .collect(Collectors.toList());
    }
    private String getFunctionStr(Message.FunctionCall functionCall) {
        return functionCall.getName() == null ? functionCall.getArguments() :
                functionCall.getName() + functionCall.getArguments();
    }


}
