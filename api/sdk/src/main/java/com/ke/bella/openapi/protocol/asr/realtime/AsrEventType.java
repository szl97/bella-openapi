package com.ke.bella.openapi.protocol.asr.realtime;

import lombok.Getter;

@Getter
public enum AsrEventType {
    // 会话开始事件
    TRANSCRIPTION_STARTED("TranscriptionStarted", true, true),
    // 句子开始事件
    SENTENCE_BEGIN("SentenceBegin", true, true),
    // 中间识别结果事件
    TRANSCRIPTION_RESULT_CHANGED("TranscriptionResultChanged", true, true),
    // 句子结束事件
    SENTENCE_END("SentenceEnd", true, true),
    // 转录完成事件
    TRANSCRIPTION_COMPLETED("TranscriptionCompleted", true, true),
    // 转录失败事件
    TRANSCRIPTION_FAILED("TranscriptionFailed", true, false),
    // 任务失败事件
    TASK_FAILED("TaskFailed", true, false),
    // 未知事件
    UNKNOWN("Unknown", true, false),
    //开始转录请求
    START_TRANSCRIPTION("StartTranscription", false, true),
    //结束转录请求
    STOP_TRANSCRIPTION("StopTranscription", false, true),
    ;

    private final String value;
    private final boolean server;
    private final boolean success;

    AsrEventType(String value, boolean send, boolean success) {
        this.value = value;
        this.server = send;
        this.success = success;
    }

    public static AsrEventType fromString(String text) {
        for (AsrEventType type : AsrEventType.values()) {
            if (type.value.equalsIgnoreCase(text)) {
                return type;
            }
        }
        return UNKNOWN;
    }
}
