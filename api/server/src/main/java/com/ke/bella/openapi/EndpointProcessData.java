package com.ke.bella.openapi;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EndpointProcessData {
    private String requestId;
    private String accountType;
    private String accountCode;
    private String akCode;
    private String endpoint;
    private String model;
    private String channelCode;
    private String user;
    private long requestTime;
    private long firstPackageTime;
    private long duration;
    private Object request;
    private Object response;
    private Object usage;
    private Object metrics;
    private String forwardUrl;
    private String protocol;
    private String priceInfo;
    private String channelInfo;
    private String supplier;
    private Object requestRiskData;
}
