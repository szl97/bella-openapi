package com.ke.bella.openapi.protocol.asr.realtime;

import com.ke.bella.openapi.protocol.OpenapiResponse;
import lombok.Data;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;

/**
 * 实时语音识别消息
 * 用于所有ASR服务的请求和响应
 */
@Data
public class RealTimeAsrMessage extends OpenapiResponse {
    private static final long serialVersionUID = 1L;


    public static RealTimeAsrMessage startedResponse(String taskId) {
        RealTimeAsrMessage response = new RealTimeAsrMessage();
        response.setHeader(RealTimeAsrHeader.header(AsrEventType.TRANSCRIPTION_STARTED, taskId));
        return response;
    }

    public static RealTimeAsrMessage errorResponse(int httpCode, int status,  String errorMessage, String taskId) {
        RealTimeAsrMessage response = new RealTimeAsrMessage();
        OpenapiError error = OpenapiError.builder().httpCode(httpCode).code(String.valueOf(status)).message(errorMessage).build();
        response.setError(error);
        response.setHeader(RealTimeAsrHeader.errorHeader(status, errorMessage, taskId));
        return response;
    }

    public static RealTimeAsrMessage sentenceBegin(String taskId, Payload payload) {
        RealTimeAsrMessage response = new RealTimeAsrMessage();
        response.setHeader(RealTimeAsrHeader.header(AsrEventType.SENTENCE_BEGIN, taskId));
        response.setPayload(payload);
        return response;
    }

    public static RealTimeAsrMessage resultChange(String taskId, Payload payload) {
        RealTimeAsrMessage response = new RealTimeAsrMessage();
        response.setHeader(RealTimeAsrHeader.header(AsrEventType.TRANSCRIPTION_RESULT_CHANGED, taskId));
        response.setPayload(payload);
        return response;
    }

    public static RealTimeAsrMessage SentenceEnd(String taskId, Payload payload) {
        RealTimeAsrMessage response = new RealTimeAsrMessage();
        response.setHeader(RealTimeAsrHeader.header(AsrEventType.SENTENCE_END, taskId));
        response.setPayload(payload);
        return response;
    }

    public static RealTimeAsrMessage completion(String taskId) {
        RealTimeAsrMessage response = new RealTimeAsrMessage();
        response.setHeader(RealTimeAsrHeader.header(AsrEventType.TRANSCRIPTION_COMPLETED, taskId));
        return response;
    }
    
    /**
     * 消息头
     * name字段决定消息类型:
     * - StartTranscription: 开始转录请求 (client)
     * - StopTranscription: 停止转录请求 (client)
     * - TranscriptionStarted: 转录已开始响应
     * - SentenceBegin: 句子开始
     * - TranscriptionResultChanged: 中间转录结果
     * - SentenceEnd: 句子结束
     * - TranscriptionCompleted: 转录完成响应
     * - TranscriptionFailed: 转录失败响应
     */
    private RealTimeAsrHeader header;
    
    /**
     * 上下文信息
     * 可用于传递额外的信息
     */
    private Object context;
    
    /**
     * 消息负载
     * 根据消息类型不同而有不同的内容
     */
    private Payload payload;

    
    /**
     * 消息负载
     * 包含识别结果或音频格式参数
     */
    @Data
    public static class Payload implements Serializable {
        private static final long serialVersionUID = 1L;
        
        // === 响应消息中使用的字段 ===
        
        /**
         * 句子编号，从0或1开始递增
         * 在SentenceBegin, TranscriptionResultChanged, SentenceEnd消息中使用
         */
        private Integer index;
        
        /**
         * 当前已处理的音频时长，单位是毫秒
         * 在SentenceBegin, TranscriptionResultChanged, SentenceEnd消息中使用
         */
        private Integer time;
        
        /**
         * 当前句子对应的SentenceBegin事件的时间，单位是毫秒
         * 在SentenceEnd消息中使用
         */
        private Integer begin_time;
        
        /**
         * 当前的识别结果
         * 在TranscriptionResultChanged, SentenceEnd消息中使用
         */
        private String result;

        
        /**
         * 当前句子的词信息列表
         * 在TranscriptionResultChanged, SentenceEnd消息中使用
         */
        private List<Word> words;
        
        /**
         * 当前句子识别结果的置信度，取值范围：[0.0,1.0]
         * 在TranscriptionResultChanged, SentenceEnd消息中使用
         */
        private Double confidence;
        
        /**
         * 当前句子的情感
         * 包含positive(正面情感)、negative(负面情感)、neutral(无明显情感)三种类别
         * 在SentenceEnd消息中使用
         */
        private String emo_tag;
        
        /**
         * 当前句子识别情感的置信度，取值范围：[0.0,1.0]
         * 在SentenceEnd消息中使用
         */
        private Double emo_confidence;
        
        /**
         * 会话ID
         * 在TranscriptionStarted消息中使用
         */
        private String session_id;
        
        // === 请求消息中使用的字段 ===
        
        /**
         * 音频格式
         * 在StartTranscription请求中使用
         */
        private String format;
        
        /**
         * 采样率
         * 在StartTranscription请求中使用
         */
        private Integer sample_rate;
        
        /**
         * 是否启用中间结果
         * 在StartTranscription请求中使用
         */
        private Boolean enable_intermediate_result;
        
        /**
         * 是否启用标点预测
         * 在StartTranscription请求中使用
         */
        private Boolean enable_punctuation_prediction;
        
        /**
         * 是否启用逆文本规范化
         * 在StartTranscription请求中使用
         */
        private Boolean enable_inverse_text_normalization;
    }
    
    /**
     * 词信息
     */
    @Data
    public static class Word implements Serializable {
        private static final long serialVersionUID = 1L;
        
        /**
         * 文本
         */
        private String text;
        
        /**
         * 词开始时间，单位为毫秒
         */
        private Integer startTime;
        
        /**
         * 词结束时间，单位为毫秒
         */
        private Integer endTime;
        
        /**
         * 词的置信度
         */
        private Double probability;
    }
}
