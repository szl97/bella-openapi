package com.ke.bella.openapi.protocol.completion;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

import static com.ke.bella.openapi.protocol.completion.StreamCompletionResponse.CHAT_COMPLETION_CHUNK_OBJECT;

public class ResponseHelper {

    public static CompletionResponse overwrite(CompletionResponse response, StreamCompletionResponse streamCompletionResponse) {
        response.setError(streamCompletionResponse.getError());
        response.setCreated(streamCompletionResponse.getCreated());
        response.setId(streamCompletionResponse.getId());
        response.setModel(streamCompletionResponse.getModel());
        response.setUsage(streamCompletionResponse.getUsage());
        response.setObject(CHAT_COMPLETION_CHUNK_OBJECT);
        return response;
    }

    public static CompletionResponse.Choice convert(StreamCompletionResponse.Choice streamChoice) {
        CompletionResponse.Choice choice = new CompletionResponse.Choice();
        choice.setIndex(streamChoice.getIndex());
        choice.setFinish_reason(streamChoice.getFinish_reason());
        choice.setMessage(streamChoice.getDelta());
        return choice;
    }

    public static Message combineMessage(Message target, Message message) {
        if(target == null) {
            return message;
        }
        Object contentObj = message.getContent();
        List<Message.ToolCall> toolCallList = message.getTool_calls();
        if(contentObj != null) {
            String content = contentObj.toString();
            if(target.getContent() == null) {
                target.setContent(content);
            } else {
                target.setContent(target.getContent() + content);
            }
        } else if(CollectionUtils.isNotEmpty(toolCallList)) {
            if(target.getTool_calls() == null) {
                target.setTool_calls(toolCallList);
            } else {
                for (Message.ToolCall streamToolCall : toolCallList) {
                    //拼接对应index的function
                    int toolIndex = streamToolCall.getIndex();
                    String name = streamToolCall.getFunction().getName();
                    String arguments = streamToolCall.getFunction().getArguments();
                    if(StringUtils.isBlank(name) && StringUtils.isEmpty(arguments)) {
                        return target;
                    }
                    boolean add = true;
                    for (Message.ToolCall toolCall : target.getTool_calls()) {
                        if(toolCall.getIndex() == toolIndex) {
                            add = false;
                            if(StringUtils.isNotBlank(name) && StringUtils.isBlank(toolCall.getFunction().getName())) {
                                toolCall.getFunction().setName(name);
                            }
                            if(toolCall.getFunction().getArguments() == null) {
                                toolCall.getFunction().setArguments(arguments);
                            } else {
                                toolCall.getFunction().setArguments(toolCall.getFunction().getArguments() + arguments);
                            }
                        }
                    }
                    if(add) {
                        target.getTool_calls().add(streamToolCall);
                    }
                }
            }
        }
        return target;
    }
}
