package com.ke.bella.openapi.protocol.completion;

import com.ke.bella.openapi.protocol.IPriceInfo;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CompletionPriceInfo implements IPriceInfo {
    private BigDecimal input;
    private BigDecimal output;
    private String unit = "分/千token";
}
