package com.ke.bella.openapi.protocol.asr;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.ke.bella.openapi.protocol.IPriceInfo;
import lombok.Data;

@Data
public class AsrFlashPriceInfo implements IPriceInfo, Serializable  {

    private BigDecimal price;

    @Override
    public String getUnit() {
        return "分/次";
    }

    @Override
    public Map<String, String> description() {
        return ImmutableMap.of("price", "分/次");
    }
}
