package com.ke.bella.openapi;

import com.ke.bella.openapi.apikey.ApikeyInfo;
import com.ke.bella.openapi.protocol.OpenapiResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EndpointProcessData {
    private String apikey;
    private String requestId;
    private String accountType;
    private String accountCode;
    private String akCode;
    private String parentAkCode;
    private String endpoint;
    private String model;
    private String channelCode;
    private boolean isPrivate;
    private String user;
    //时间单位都为s
    private long requestTime;
    private long firstPackageTime;
    private long duration;
    private Object request;
    private OpenapiResponse response;
    private Object usage;
    private Map<String, Object> metrics;
    private String forwardUrl;
    private String protocol;
    private String priceInfo;
    private String encodingType;
    private String supplier;
    private Object requestRiskData;
    private boolean isMock;
    private String bellaTraceId;
    private boolean functionCallSimulate;
    private String channelRequestId;
    private BigDecimal cost;
    private boolean innerLog;
    private Integer maxWaitSec;

    public void setApikeyInfo(ApikeyInfo ak) {
        this.setApikey(ak.getApikey());
        this.setAkCode(ak.getCode());
        this.setParentAkCode(ak.getParentCode());
        this.setAccountType(ak.getOwnerType());
        this.setAccountCode(ak.getOwnerCode());
    }
}
