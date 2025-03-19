package com.ke.bella.openapi.protocol.asr;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class HuoshanRealTimeAsrResponse {
    private int code;
    private String message;
    private int sequence;
    private java.util.List<Result> result;

    @Data
    @NoArgsConstructor
    public static class Result {
        private String text;
        private double confidence;
        private java.util.List<Word> words;

        @Data
        @NoArgsConstructor
        public static class Word {
            private String text;
            private double confidence;
            private long begin_time;
            private long end_time;
        }
    }
}
