package com.ke.bella.openapi.protocol.completion;

import com.ke.bella.openapi.utils.SseHelper;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.List;

import static com.ke.bella.openapi.protocol.completion.StreamCompletionResponse.CHAT_COMPLETION_CHUNK_OBJECT;

public class ResponseHelper {
    public static final String START_THINK = "<think>";
    public static final String END_THINK = "</think>";

    public static void splitReasoningFromContent(CompletionResponse rsp, OpenAIProperty property) {
        if(!property.splitReasoningFromContent) {
            return;
        }
        if(CollectionUtils.isEmpty(rsp.getChoices()) || rsp.getChoices().get(0).getMessage() == null
                || ObjectUtils.isEmpty(rsp.getChoices().get(0).getMessage().getContent())) {
            return;
        }
        String content = rsp.getChoices().get(0).getMessage().getContent().toString();
        if(!content.startsWith(START_THINK)) {
            return;
        }
        String[] parts = content.split(END_THINK);
        if(parts.length != 2) {
            return;
        }
        String reasonContent = parts[0].replace(START_THINK, "");
        content = parts[1];
        rsp.getChoices().get(0).getMessage().setReasoning_content(reasonContent);
        rsp.getChoices().get(0).getMessage().setContent(content);
    }

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
        choice.setMessage(combineMessage(null, streamChoice.getDelta()));
        return choice;
    }

    public static Message combineMessage(Message target, Message message) {
        if(target == null) {
            target = new Message();
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
        } else if(StringUtils.isNotEmpty(message.getReasoning_content())) {
            if(target.getReasoning_content() == null) {
                target.setReasoning_content(message.getReasoning_content());
            } else {
                target.setReasoning_content(target.getReasoning_content() + message.getReasoning_content());
            }
        } else if(CollectionUtils.isNotEmpty(toolCallList)) {
            if(target.getTool_calls() == null) {
                target.setTool_calls(new ArrayList<>());
            }
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
                    target.getTool_calls().add(copyToolCall(streamToolCall));
                }
            }
        }
        return target;
    }

    public static StreamCompletionResponse rebuildThinkResp(StreamCompletionResponse thinkResp, Integer stage) {
        StreamCompletionResponse response = new StreamCompletionResponse();
        response.setCreated(thinkResp.getCreated());
        response.setChoices(new ArrayList<>());
        response.setUsage(thinkResp.getUsage());
        response.setId(thinkResp.getId());
        response.setModel(thinkResp.getModel());
        Message thinkMessage = thinkResp.getChoices().get(0).getDelta();
        Message message = new Message();
        message.setName(thinkMessage.getName());
        message.setContent(thinkMessage.getReasoning_content());
        message.setReasoning_content(thinkMessage.getReasoning_content());
        if(stage == 0) {
            message.setContent("```sh\n" + message.getContent());
        } else if(stage == -1) {
            message.setContent("\n```\n" + thinkMessage.getContent());
        }
        response.getChoices().add(new StreamCompletionResponse.Choice("", 0, message));
        return response;
    }

    public static Message.ToolCall copyToolCall(Message.ToolCall toolCall) {
        return new Message.ToolCall(toolCall.getIndex(), toolCall.getId(), toolCall.getType(),
                Message.FunctionCall.builder().name(toolCall.getFunction().getName()).arguments(toolCall.getFunction().getArguments()).build());
    }

}
