package com.ke.bella.openapi.protocol.completion;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.ke.bella.openapi.protocol.IPriceInfo;
import lombok.Data;

import java.math.BigDecimal;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CompletionPriceInfo implements IPriceInfo {
    private BigDecimal input;
    private BigDecimal output;
    private String unit = "分/千token";
}
