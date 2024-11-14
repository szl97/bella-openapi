package com.ke.bella.openapi.protocol.completion;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.common.collect.ImmutableSortedMap;
import com.ke.bella.openapi.protocol.IPriceInfo;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CompletionPriceInfo implements IPriceInfo {
    private static final long serialVersionUID = 1L;
    private BigDecimal input;
    private BigDecimal output;
    private String unit = "分/千token";

    @Override
    public Map<String, String> priceDescription() {
        return ImmutableSortedMap.of("input", "输入token单价", "output", "输出token单价");
    }
}
