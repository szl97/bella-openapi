package com.ke.bella.openapi.protocol.completion;

import com.google.common.collect.ImmutableMap;
import com.ke.bella.openapi.protocol.IProtocolProperty;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

@Data
public class CompletionProperty implements IProtocolProperty {
    String encodingType = StringUtils.EMPTY;
    boolean mergeReasoningContent = false;
    boolean splitReasoningFromContent = false;

    @Override
    public Map<String, String> description() {
        return ImmutableMap.of("encodingType", "编码类型");
    }
}
