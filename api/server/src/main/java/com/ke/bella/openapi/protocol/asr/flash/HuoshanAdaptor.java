package com.ke.bella.openapi.protocol.asr.flash;

import java.util.concurrent.CompletableFuture;

import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;
import com.ke.bella.openapi.EndpointProcessData;
import com.ke.bella.openapi.common.exception.ChannelException;
import com.ke.bella.openapi.protocol.BellaWebSocketListener;
import com.ke.bella.openapi.protocol.Callbacks;
import com.ke.bella.openapi.protocol.asr.AsrFlashResponse;
import com.ke.bella.openapi.protocol.asr.AsrRequest;
import com.ke.bella.openapi.protocol.asr.HuoshanProperty;
import com.ke.bella.openapi.protocol.asr.HuoshanRealTimeAsrRequest;
import com.ke.bella.openapi.protocol.asr.HuoshanRealTimeAsrResponse;
import com.ke.bella.openapi.protocol.asr.HuoshanStreamAsrCallback;
import com.ke.bella.openapi.utils.HttpUtils;

import okhttp3.Request;

@Component("HuoshanFlashAsr")
public class HuoshanAdaptor implements FlashAsrAdaptor<HuoshanProperty> {

    @Override
    public AsrFlashResponse asr(AsrRequest request, String url, HuoshanProperty property, EndpointProcessData processData) {
        HuoshanRealTimeAsrRequest huoshanRequest = new HuoshanRealTimeAsrRequest(request, property, false);
        CompletableFuture<String> future = new CompletableFuture();
        Callbacks.TextSender sender = buildSender(future);
        HuoshanStreamAsrCallback callback = new HuoshanStreamAsrCallback(huoshanRequest, sender, processData, null, HuoshanRealTimeAsrResponse.Result::getText);
        Request httpRequest = authorizationRequestBuilder(property.getAuth()).url(url).build();
        HttpUtils.websocketRequest(httpRequest, new BellaWebSocketListener(callback));
        return responseConverter(future, processData);
    }

    @Override
    public String getDescription() {
        return "火山协议";
    }

    @Override
    public Class<?> getPropertyClass() {
        return HuoshanProperty.class;
    }

    private Callbacks.TextSender buildSender(CompletableFuture<String> future) {
        return new Callbacks.TextSender() {
            String text;
            @Override
            public void send(String text) {
                this.text = text;
            }

            @Override
            public void onError(Throwable e) {
                future.completeExceptionally(e);
            }

            @Override
            public void close() {
                if(!future.isDone()) {
                future.complete(text);
                }
            }
        };
    }

    private AsrFlashResponse responseConverter(CompletableFuture<String> future, EndpointProcessData processData) {
        try {
            String text = future.get();
            return AsrFlashResponse.builder()
                    .taskId(processData.getChannelRequestId())
                    .flashResult(AsrFlashResponse.FlashResult.builder()
                            .duration(Integer.parseInt(processData.getMetrics().get("ttlt").toString()))
                            .sentences(Lists.newArrayList(AsrFlashResponse.Sentence.builder().text(text).build()))
                            .build())
                    .user(processData.getUser())
                    .build();
        } catch (Exception e) {
            throw ChannelException.fromException(e);
        }
    }

}
