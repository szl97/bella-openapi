package com.ke.bella.openapi.protocol.embedding;

import com.google.common.collect.ImmutableMap;
import com.ke.bella.openapi.protocol.IPriceInfo;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
public class EmbeddingPriceInfo implements IPriceInfo {
    private static final long serialVersionUID = 1L;
    private BigDecimal input;
    private String unit = "分/千token";

    @Override
    public Map<String, String> priceDescription() {
        return ImmutableMap.of("input", "输入token单价");
    }
}
