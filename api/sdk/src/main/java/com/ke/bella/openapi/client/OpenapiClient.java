package com.ke.bella.openapi.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.net.HttpHeaders;
import com.ke.bella.openapi.BellaResponse;
import com.ke.bella.openapi.apikey.ApikeyInfo;
import com.ke.bella.openapi.common.exception.ChannelException;
import com.ke.bella.openapi.utils.HttpUtils;
import okhttp3.Request;
import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class OpenapiClient {
    private final String openapiHost;
    private Cache<String, ApikeyInfo> cache = CacheBuilder.newBuilder()
            .expireAfterAccess(10, TimeUnit.MINUTES)
            .maximumSize(10000)
            .build();

    public OpenapiClient(String openapiHost) {
        this.openapiHost = openapiHost;
    }

    public ApikeyInfo whoami(String apikey) {
        try {
            ApikeyInfo apikeyInfo = cache.get(apikey, () -> requestApikeyInfo(apikey));
            if(StringUtils.isEmpty(apikeyInfo.getCode())) {
                return null;
            }
            return apikeyInfo;
        } catch (ExecutionException e) {
            throw ChannelException.fromException(e);
        }
    }
    public boolean validate(String apikey) {
        return whoami(apikey) != null;
    }

    public boolean hasPermission(String apikey, String url) {
        ApikeyInfo apikeyInfo = whoami(apikey);
        if(apikeyInfo != null) {
            return apikeyInfo.hasPermission(url);
        }
        return false;
    }

    private ApikeyInfo requestApikeyInfo(String apikey) {
        if(StringUtils.isEmpty(apikey)) {
            return null;
        }
        String url = openapiHost + "/v1/apikey/whoami";
        Request request = new Request.Builder()
                .url(url)
                .addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apikey)
                .build();
        BellaResponse<ApikeyInfo> bellaResp = HttpUtils.httpRequest(request, new TypeReference<BellaResponse<ApikeyInfo>>() {});
        return bellaResp == null || bellaResp.getData() == null ? new ApikeyInfo() : bellaResp.getData();
    }
}
