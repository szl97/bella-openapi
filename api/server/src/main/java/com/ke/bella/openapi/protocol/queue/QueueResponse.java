package com.ke.bella.openapi.protocol.queue;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

public class QueueResponse {
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QueueTaskPutResp {

        @JsonProperty("task_id")
        private String taskId;

    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QueueTaskGetResultResp {

        private List<Object> data;

    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TaskGetDetailResp {

        private DetailData data;

    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DetailData {
        private String endpoint;
        private String model;
        @JsonProperty("input_data")
        private Object inputData;
        private String status;
        private String ak;
        @JsonProperty("input_fileid")
        private String inputFileId;
        @JsonProperty("output_fileid")
        private String outputFileId;
        @JsonProperty("output_data")
        private Object outputData;
    }
}
