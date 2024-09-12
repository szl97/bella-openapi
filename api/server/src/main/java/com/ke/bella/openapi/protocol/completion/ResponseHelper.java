package com.ke.bella.openapi.protocol.completion;

import org.apache.commons.collections4.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

import static com.ke.bella.openapi.protocol.completion.StreamCompletionResponse.CHAT_COMPLETION_CHUNK_OBJECT;

public class ResponseHelper {

    public static CompletionResponse convert(StreamCompletionResponse streamCompletionResponse) {
        CompletionResponse response = new CompletionResponse();
        response.setError(streamCompletionResponse.getError());
        response.setCreated(streamCompletionResponse.getCreated());
        response.setId(streamCompletionResponse.getId());
        response.setModel(streamCompletionResponse.getModel());
        response.setUsage(streamCompletionResponse.getUsage());
        response.setObject(CHAT_COMPLETION_CHUNK_OBJECT);
        response.setChoices(streamCompletionResponse.getChoices().stream().map(ResponseHelper::convert).collect(Collectors.toList()));
        return response;
    }

    public static CompletionResponse.Choice convert(StreamCompletionResponse.Choice streamChoice) {
        CompletionResponse.Choice choice = new CompletionResponse.Choice();
        choice.setIndex(streamChoice.getIndex());
        choice.setFinish_reason(streamChoice.getFinish_reason());
        choice.setMessage(streamChoice.getDelta());
        return choice;
    }

    public static CompletionResponse aggregate(List<StreamCompletionResponse> list) {
        CompletionResponse response = null;
        for (StreamCompletionResponse streamResponse : list) {
            if(CollectionUtils.isEmpty(streamResponse.getChoices())) {
                continue;
            }
            if(response == null) {
                response = ResponseHelper.convert(streamResponse);
            } else {
                int index = streamResponse.getChoices().get(0).getIndex();
                String content = (String) streamResponse.getChoices().get(0).getDelta().getContent();
                //拼接当前choice内容
                //判断当前choice对应的index是否已存在
                boolean newChoice = true;
                for (CompletionResponse.Choice choice : response.getChoices()) {
                    if(choice.getIndex() == index) {
                        newChoice = false;
                        if(content != null) {
                            if(choice.getMessage().getContent() == null) {
                                choice.getMessage().setContent(content);
                            } else {
                                choice.getMessage().setContent(choice.getMessage().getContent() + content);
                            }
                        } else if(CollectionUtils.isNotEmpty(streamResponse.getChoices().get(0).getDelta().getTool_calls())) {
                            //拼接function的arguments
                            //拼接对应index的function
                            int toolIndex = streamResponse.getChoices().get(0).getDelta().getTool_calls().get(0).getIndex();
                            String arguments = streamResponse.getChoices().get(0).getDelta().getTool_calls().get(0).getFunction().getArguments();
                            if(arguments == null) {
                                continue;
                            }
                            for (Message.ToolCall toolCall : choice.getMessage().getTool_calls()) {
                                if(toolCall.getIndex() == toolIndex) {
                                    if(toolCall.getFunction().getArguments() == null) {
                                        toolCall.getFunction().setArguments(arguments);
                                    } else {
                                        toolCall.getFunction().setArguments(toolCall.getFunction().getArguments() + arguments);
                                    }
                                }
                            }
                        }
                    }
                }
                if(newChoice) {
                    response.getChoices().add(ResponseHelper.convert(streamResponse.getChoices().get(0)));
                }
                response.setUsage(streamResponse.getUsage());
            }
        }
        return response;
    }
}
