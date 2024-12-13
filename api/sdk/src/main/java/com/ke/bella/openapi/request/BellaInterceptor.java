package com.ke.bella.openapi.request;

import com.ke.bella.openapi.BellaContext;
import lombok.AllArgsConstructor;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.collections4.MapUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.util.Assert;

import java.io.IOException;
import java.util.Map;

@AllArgsConstructor
public class BellaInterceptor implements Interceptor {
    private Map<String, Object> context;
    @NotNull
    @Override
    public Response intercept(@NotNull Chain chain) throws IOException {
        Map<String, String> headers = (Map<String, String>) context.get("headers");
        String traceId = headers.get(BellaContext.BELLA_TRACE_HEADER);
        Assert.notNull(traceId, "traceId is empty");
        Request originalRequest = chain.request();
        Request.Builder bellaRequest = originalRequest.newBuilder();
        if(MapUtils.isNotEmpty(headers)) {
            headers.forEach(bellaRequest::header);
        }
        return chain.proceed(bellaRequest.build());
    }
}
