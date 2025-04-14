package com.ke.bella.openapi.protocol.tts;

import com.google.common.collect.ImmutableSortedMap;
import com.ke.bella.openapi.protocol.IModelProperties;
import lombok.Data;

import java.util.Map;

@Data
public class VoiceProperties implements IModelProperties {

    private Map<String, String> voiceTypes;

    @Override
    public Map<String, String> description() {
        return ImmutableSortedMap.of("voiceTypes", "声音类型");
    }
}
