package com.ke.bella.openapi.db;

import com.google.common.collect.ImmutableList;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

import static com.ke.bella.openapi.db.TableConstants.SystemBasicEndpoint.COMPLETION_ENDPOINT;

/**
 * Author: Stan Sai Date: 2024/8/1 20:46 description:
 */
public class TableConstants {
    public static final String ACTIVE = "active";
    public static final String INACTIVE = "inactive";
    public static final String PUBLIC = "public";
    public static final String PRIVATE = "private";
    public static final String INNER = "inner";
    public static final String MAINLAND = "mainland";
    public static final String OVERSEAS = "overseas";
    public static final List<String> DATA_DESTINATIONS = ImmutableList.of(INNER, MAINLAND, OVERSEAS);
    public static final String HIGH = "high";
    public static final String NORMAL = "normal";
    public static final String LOW = "low";
    public static final List<String> CHANNEL_PRIORITY = ImmutableList.of(HIGH, NORMAL, LOW);
    public static final String MODEL = "model";
    public static final String ENDPOINT = "endpoint";
    public static final List<String> ENTITY_TYPES = ImmutableList.of(MODEL, ENDPOINT);

    @AllArgsConstructor
    @Getter
    public enum SystemBasicEndpoint {
        COMPLETION_ENDPOINT("/v*/chat/completions", "智能问答", "chat"),
        EMBEDDING_ENDPOINT("/v*/embeddings", "向量化", "chat"),
        SPEECH_ENDPOINT("/v*/audio/speech", "语音合成", "audio_text2Speech"),
        ASR_ENDPOINT("/api/v*/asr/starttask", "语音识别", "audio_audio2Text"),
        TEXT2IMAGE_ENDPOINT("/v*/images/generations", "文生图", "images"),
        IMAGE2IMAGE_ENDPOINT("/v*/images/edits", "图生图", "images"),
        ;
        private final String endpoint;
        private final String name;
        private final String category;
    }

    @AllArgsConstructor
    @Getter
    public enum SystemBasicCategory {
        CHAT("chat", "语言类", null),
        AUDIO("audio", "语音类", null),
        IMAGES("images", "图像类", null),
        TEXT2SPEECH("audio_text2Speech", "语音合成", "audio"),
        AUDIO2TEXT("audio_audio2Text", "语音识别", "audio"),
        ;
        private final String code;
        private final String name;
        private final String parent;
    }

    @AllArgsConstructor
    @Getter
    public enum ModelJsonKey {
        MAX_INPUT(COMPLETION_ENDPOINT.endpoint, "properties", "max_input_context", Integer.class, "最大输入"),
        MAX_OUTPUT(COMPLETION_ENDPOINT.endpoint, "properties", "max_output_context", Integer.class, "最大输出"),
        FUNCTION_CALL(COMPLETION_ENDPOINT.endpoint, "features", "function_call", Boolean.class, "是否支持工具调用"),
        STEAM(COMPLETION_ENDPOINT.endpoint, "features", "stream", Boolean.class, "是否支持流式"),
        STREAM_FUNCTION_CALL(COMPLETION_ENDPOINT.endpoint, "features", "stream_function_call", Boolean.class, "是否支持流式工具调用"),
        PARALLEL_TOOL_CALLS(COMPLETION_ENDPOINT.endpoint, "features", "parallel_tool_calls", Boolean.class, "是否支持并行工具调用"),
        VISION(COMPLETION_ENDPOINT.endpoint, "features", "vision", Boolean.class, "是否支持图片输入"),
        ;
        private final String endpoint;
        private final String fied;
        private final String code;
        private final Class<?> type;
        private final String description;
    }

}
