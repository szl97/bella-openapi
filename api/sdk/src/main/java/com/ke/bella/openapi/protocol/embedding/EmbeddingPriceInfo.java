package com.ke.bella.openapi.protocol.embedding;

import com.ke.bella.openapi.protocol.IPriceInfo;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class EmbeddingPriceInfo implements IPriceInfo {
    private BigDecimal input;
    private String unit = "分/千token";
}
