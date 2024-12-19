package com.ke.bella.openapi.protocol.queue;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import java.util.List;

public class QueueRequest {
    @Data
    @Builder
    public static class QueueTaskPutReq {

        @NotEmpty
        private String endpoint;

        @NotEmpty
        private String model;

        private Object data;

    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QueueTaskGetResultReq {

        @JsonProperty("task_id")
        private String taskId;

    }
}
