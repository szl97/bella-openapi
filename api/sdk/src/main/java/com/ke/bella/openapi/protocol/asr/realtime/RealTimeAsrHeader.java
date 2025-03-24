package com.ke.bella.openapi.protocol.asr.realtime;

import lombok.Data;
import java.io.Serializable;
import java.util.UUID;

/**
 * 实时语音识别消息头
 */
@Data
public class RealTimeAsrHeader implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String name;
    private String message_id;
    private String task_id;
    private String namespace;
    private String appkey;
    private Integer status;
    private String status_message;

    public static RealTimeAsrHeader header(AsrEventType event, String taskId) {
        RealTimeAsrHeader header = new RealTimeAsrHeader();
        header.setName(event.getValue());
        header.setMessage_id(UUID.randomUUID().toString());
        header.setTask_id(taskId);
        header.setStatus(event.isSuccess() ? 20000000 : 50000000);
        header.setStatus_message(event.isSuccess() ? "GATEWAY|SUCCESS|Success." : "FAIL");
        return header;
    }

    public static RealTimeAsrHeader errorHeader(int status, String errorMessage, String taskId) {
        RealTimeAsrHeader header = new RealTimeAsrHeader();
        header.setName(AsrEventType.TRANSCRIPTION_FAILED.getValue());
        header.setMessage_id(UUID.randomUUID().toString());
        header.setTask_id(taskId != null ? taskId : "");
        header.setStatus(status);
        header.setStatus_message(errorMessage);
        return header;
    }
}
