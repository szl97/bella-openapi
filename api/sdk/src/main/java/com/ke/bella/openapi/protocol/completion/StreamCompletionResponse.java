package com.ke.bella.openapi.protocol.completion;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.ke.bella.openapi.protocol.OpenapiResponse;
import com.ke.bella.openapi.protocol.completion.Message.ToolCall;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StreamCompletionResponse extends OpenapiResponse {
    public static final String CHAT_COMPLETION_CHUNK_OBJECT = "chat.completion.chunk";

    /**
     * 时间戳
     */
    private long created;
    /**
     * 唯一id
     */
    private String id;
    /**
     * 调用模型
     */
    private String model;

    private CompletionResponse.TokenUsage usage;

    private List<Choice> choices;

    public String reasoning() {
        if(CollectionUtils.isNotEmpty(choices)
                && choices.get(0).getDelta() != null
                && choices.get(0).getDelta().getReasoning_content() != null) {
            return choices.get(0).getDelta().getReasoning_content();
        }
        return "";
    }

    public String content() {
        if(CollectionUtils.isNotEmpty(choices)
                && choices.get(0).getDelta() != null
                && choices.get(0).getDelta().getContent() != null) {
            return choices.get(0).getDelta().getContent().toString();
        } else {
            return "";
        }
    }

    public String finishReason() {
        if(CollectionUtils.isNotEmpty(choices)
                && choices.get(0).getFinish_reason() != null) {
            return choices.get(0).getFinish_reason();
        } else {
            return "";
        }
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Choice {

        /**
         * Every response will include a finish_reason. The possible values for
         * finish_reason are:
         * <p>
         * stop: API returned complete message, or a message terminated by one
         * of the stop sequences provided via the stop parameter length:
         * Incomplete model output due to max_tokens parameter or token limit
         * function_call: The model decided to call a function content_filter:
         * Omitted content due to a
         * flag from our content filters null: API response still in progress or
         * incomplete
         */
        private String finish_reason;
        private int index;
        private Message delta;
    }

    public static List<Choice> toolcallChoice(ToolCall fc) {
        Choice choice = Choice.builder()
                .delta(Message.builder()
                        .role("tool")
                        .tool_calls(Arrays.asList(fc))
                        .tool_call_id(fc.getId())
                        .build())
                .build();
        return Arrays.asList(choice);

    }

    public static List<Choice> assistantMessageChoice(String delta) {
        Choice choice = Choice.builder()
                .delta(Message.builder()
                        .role("assistant")
                        .content(delta)
                        .build())
                .build();
        return Arrays.asList(choice);
    }

}
