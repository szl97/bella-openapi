package com.ke.bella.openapi.protocol.asr.flash;

import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import com.ke.bella.openapi.utils.DateTimeUtils;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;
import com.ke.bella.openapi.EndpointProcessData;
import com.ke.bella.openapi.common.exception.ChannelException;
import com.ke.bella.openapi.protocol.BellaWebSocketListener;
import com.ke.bella.openapi.protocol.Callbacks;
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
    public FlashAsrResponse asr(AsrRequest request, String url, HuoshanProperty property, EndpointProcessData processData) {
        HuoshanRealTimeAsrRequest huoshanRequest = new HuoshanRealTimeAsrRequest(request, property);
        CompletableFuture<String> future = new CompletableFuture();
        Callbacks.TextSender sender = buildSender(future);
        HuoshanStreamAsrCallback callback = new HuoshanStreamAsrCallback(huoshanRequest, sender, processData, null, response -> {
            if(response.getResult() == null) {
                return Lists.newArrayList();
            }
            return response.getResult().stream().map(HuoshanRealTimeAsrResponse.Result::getText).collect(Collectors.toList());
        });
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

    private FlashAsrResponse responseConverter(CompletableFuture<String> future, EndpointProcessData processData) {
        try {
            String text = future.get();
            return FlashAsrResponse.builder()
                    .taskId(processData.getChannelRequestId())
                    .flashResult(FlashAsrResponse.FlashResult.builder()
                            .duration(Integer.parseInt(processData.getMetrics().get("ttlt").toString()))
                            .sentences(Lists.newArrayList(FlashAsrResponse.Sentence.builder()
                                            .beginTime(processData.getRequestTime())
                                            .endTime(DateTimeUtils.getCurrentSeconds())
                                            .text(text).build()))
                            .build())
                    .user(processData.getUser())
                    .build();
        } catch (Exception e) {
            throw ChannelException.fromException(e);
        }
    }

}
