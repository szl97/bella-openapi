package com.ke.bella.openapi.protocol.completion;

import com.ke.bella.openapi.protocol.IProtocolProperty;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

@Data
public class CompletionProperty implements IProtocolProperty {
    String encodingType = StringUtils.EMPTY;
    boolean mergeReasoningContent = false;
    boolean splitReasoningFromContent = false;
    Map<String, String> extraHeaders;

    @Override
    public Map<String, String> description() {
        SortedMap<String, String> map = new TreeMap<>();
        map.put("encodingType", "编码类型");
        map.put("mergeReasoningContent", "是否合并推理内容");
        map.put("splitReasoningFromContent", "是否需要拆分推理内容");
        map.put("extraHeaders", "额外的请求头");
        return map;
    }
}
