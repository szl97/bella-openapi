package com.ke.bella.openapi.protocol.completion;

import com.ke.bella.openapi.protocol.IProtocolProperty;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

@Data
public class CompletionProtocolProperty implements IProtocolProperty {
    String encodingType = StringUtils.EMPTY;
}
