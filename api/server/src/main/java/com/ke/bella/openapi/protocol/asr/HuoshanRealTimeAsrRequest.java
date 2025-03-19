package com.ke.bella.openapi.protocol.asr;

import lombok.Data;

@Data
public class HuoshanRealTimeAsrRequest {
    private boolean async;
    private String uid;
    private String appId;
    private String token;
    private String cluster;
    private String format;
    private int sampleRate;
    private byte[] audioData;
    private int chunkSize;
    private int intervalMs;
    private String resultType; //full single

    public HuoshanRealTimeAsrRequest(AsrRequest request, HuoshanProperty property, boolean async) {
        this.async = async;
        this.uid = "0";
        this.appId = property.getAppid();
        this.token = property.getAuth().getSecret();
        this.cluster = property.getDeployName();
        this.format = request.getFormat();
        this.sampleRate = request.getSampleRate();
        this.audioData = request.getContent();
        this.chunkSize = property.getChunkSize();
        this.intervalMs = property.getIntervalMs();
        this.resultType = property.getResultType();
    }
}
