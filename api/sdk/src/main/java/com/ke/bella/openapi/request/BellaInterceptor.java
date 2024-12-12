package com.ke.bella.openapi.request;

import com.ke.bella.openapi.BellaContext;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.collections4.MapUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.util.Assert;

import java.io.IOException;
import java.util.Map;

public class BellaInterceptor implements Interceptor {
    @NotNull
    @Override
    public Response intercept(@NotNull Chain chain) throws IOException {
        Map<String, String> requestInfo = BellaContext.getHeaders();
        String traceId = BellaContext.getTraceId();
        Assert.notNull(traceId, "traceId is empty");
        Request originalRequest = chain.request();
        Request.Builder bellaRequest = originalRequest.newBuilder();
        if(MapUtils.isNotEmpty(requestInfo)) {
            requestInfo.forEach(bellaRequest::header);
        }
        return chain.proceed(bellaRequest.build());
    }
}
