package com.ke.bella.openapi.protocol.completion;

import java.util.List;

import org.apache.commons.collections4.CollectionUtils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.ke.bella.openapi.protocol.OpenapiResponse;
import com.ke.bella.openapi.protocol.completion.Message.ToolCall;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@JsonInclude(Include.NON_NULL)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class CompletionResponse extends OpenapiResponse {

    private List<Choice> choices;
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

    /**
     * This fingerprint represents the backend configuration that the model runs with.<br/><br/> Can be used in conjunction with the seed request
     * parameter to understand when backend changes have been made that might impact determinism.
     */
    private String system_fingerprint;

    /**
     * 调用接口
     */
    private String object;

    private TokenUsage usage;

    public String message() {
        if(CollectionUtils.isNotEmpty(choices)
                && choices.get(0).getMessage() != null
                && choices.get(0).getMessage().getContent() != null) {
            return choices.get(0).getMessage().getContent().toString();
        } else {
            return "";
        }
    }

    public String reasoning() {
        if(CollectionUtils.isNotEmpty(choices) && choices.get(0).getMessage() != null) {
            return choices.get(0).getMessage().getReasoning_content();
        } else {
            return "";
        }
    }

    public String finishReason() {
        if(CollectionUtils.isNotEmpty(choices)) {
            return choices.get(0).getFinish_reason();
        } else {
            return null;
        }
    }

    @Data
    @JsonInclude(Include.NON_NULL)
    @SuperBuilder
    @NoArgsConstructor
    public static class Choice {

        /**
         * Every response will include a finish_reason. The possible values for finish_reason are:
         * <p>
         * stop: API returned complete message, or a message terminated by one of the stop sequences provided via the stop parameter length:
         * Incomplete model output due to max_tokens parameter or token limit function_call: The model decided to call a function content_filter:
         * Omitted content due to a flag from our content filters null: API response still in progress or incomplete
         */
        private String finish_reason;
        private int index;
        private Message message;
    }

    public static Choice toolcallChoice(List<ToolCall> calls) {
        Choice c = Choice.builder()
                .message(Message.builder()
                        .role("tool")
                        .tool_calls(calls)
                        .build())
                .build();
        return c;
    }

    public static Choice assistantMessageChoice(String reasoning, String content) {
        Choice c = Choice.builder()
                .message(Message.builder()
                        .role("assistant")
                        .reasoning_content(reasoning)
                        .content(content)
                        .build())
                .build();
        return c;
    }

    @Data
    public static class TokenUsage {
        private int completion_tokens;
        private int prompt_tokens;
        private int total_tokens;

        public TokenUsage add(TokenUsage u) {
            this.completion_tokens += u.completion_tokens;
            this.prompt_tokens += u.prompt_tokens;
            this.total_tokens += u.total_tokens;
            return this;
        }
    }

}

