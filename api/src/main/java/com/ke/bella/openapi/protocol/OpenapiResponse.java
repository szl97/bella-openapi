package com.ke.bella.openapi.protocol;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class OpenapiResponse {
    /**
     * 错误
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private OpenapiError error;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Object sensitives;

    public static OpenapiError convertFromException(ChannelException channelException) {
        if(channelException instanceof ChannelException.OpenAIException) {
            return ((ChannelException.OpenAIException) channelException).getResponse();
        } else {
            return new OpenapiError(channelException.getType(), channelException.getMessage(), channelException.getType());
        }
    }

    public static OpenapiResponse errorResponse(OpenapiError error) {
        OpenapiResponse response = new OpenapiResponse();
        response.setError(error);
        return response;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class OpenapiError {

        //请求命中敏感信息后，在该字段中返回敏感内容
        private Object sensitive;
        /**
         * message通常存在
         */
        private String message;
        /**
         * type通常存在
         */
        private String type;
        /**
         * 可能为空
         */
        private String param;
        /**
         * 可能为空
         */
        private String code;

        public OpenapiError(String type, String message) {
            this.message = message;
            this.type = type;
        }

        public OpenapiError(String type, String message, String code) {
            this.message = message;
            this.type = type;
            this.code = code;
        }
    }

}
