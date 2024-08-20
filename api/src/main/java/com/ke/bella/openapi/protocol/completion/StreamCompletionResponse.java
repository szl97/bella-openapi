package com.ke.bella.openapi.protocol.completion;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.ke.bella.openapi.protocol.OpenapiResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

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
    private int created;
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

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Choice {

        /**
         * Every response will include a finish_reason. The possible values for finish_reason are:
         * <p>
         * stop: API returned complete message, or a message terminated by one of the stop sequences provided via the stop parameter length: Incomplete model output due to max_tokens parameter or token limit function_call: The model decided to call a function content_filter: Omitted content due to a
         * flag from our content filters null: API response still in progress or incomplete
         */
        private String finish_reason;
        private int index;
        private Message delta;
    }

}
