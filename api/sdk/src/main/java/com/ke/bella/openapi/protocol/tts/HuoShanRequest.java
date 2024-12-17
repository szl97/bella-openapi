package com.ke.bella.openapi.protocol.tts;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HuoShanRequest {
    private App app;
    private User user;
    private Audio audio;
    private TextRequest request;


    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class App{
        @JsonProperty("appid")
        private String appId;
        private String token;
        private String cluster = "volcano_tts";
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class User{
        private String uid;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Audio{
        @JsonProperty("voice_type")
        private String voiceType = "BV001_streaming";
        private String encoding = "wav";
        @JsonProperty("speed_ratio")
        private Double speedRatio = 1.0;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TextRequest{
        @JsonProperty("reqid")
        private String reqId;
        private String text;
        @JsonProperty("text_type")
        private String textType="";
        private String operation="query";
     }
}
