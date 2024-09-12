package com.ke.bella.openapi.apikey;

import com.ke.bella.openapi.Operator;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ApikeyCreateOp extends Operator {
    private String name;
    private String parentCode;
    private Byte safetyLevel;
    private String outEntityCode;
    private BigDecimal monthQuota;
    private String roleCode;
    private List<String> paths;
    private String remark;
}
