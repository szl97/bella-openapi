package com.ke.bella.openapi.protocol.asr.flash;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import okhttp3.WebSocket;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;
import com.ke.bella.openapi.EndpointProcessData;
import com.ke.bella.openapi.common.exception.ChannelException;
import com.ke.bella.openapi.protocol.BellaWebSocketListener;
import com.ke.bella.openapi.protocol.Callbacks;
import com.ke.bella.openapi.protocol.asr.AsrRequest;
import com.ke.bella.openapi.protocol.asr.HuoshanProperty;
import com.ke.bella.openapi.protocol.asr.HuoshanRealTimeAsrRequest;
import com.ke.bella.openapi.protocol.asr.HuoshanStreamAsrCallback;
import com.ke.bella.openapi.utils.HttpUtils;
import com.ke.bella.openapi.utils.JacksonUtils;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import okhttp3.Request;

@Component("HuoshanFlashAsr")
public class HuoshanAdaptor implements FlashAsrAdaptor<HuoshanProperty> {

    @Override
    public FlashAsrResponse asr(AsrRequest request, String url, HuoshanProperty property, EndpointProcessData processData) {
        HuoshanRealTimeAsrRequest huoshanRequest = new HuoshanRealTimeAsrRequest(request, property);
        CompletableFuture<String> future = new CompletableFuture();
        Callbacks.Sender sender = buildSender(future);
        HuoshanStreamAsrCallback callback = new HuoshanStreamAsrCallback(huoshanRequest, sender, processData, null, response -> {
            if(response.getResult() == null) {
                return Lists.newArrayList();
            }
            return response.getResult().stream()
                    .map(result -> {
                        if(CollectionUtils.isEmpty(result.getUtterances())) {
                            return Lists.newArrayList(result);
                        } else {
                            return result.getUtterances();
                        }
                    }).flatMap(List::stream)
                    .map(result -> {
                        Text text = Text.builder().beginTime(result.getBeginTime())
                                .endTime(result.getEnd_time())
                                .text(result.getText())
                                .build();
                        return JacksonUtils.serialize(text);
                    }).collect(Collectors.toList());
        });
        Request httpRequest = authorizationRequestBuilder(property.getAuth()).url(url).build();
        WebSocket webSocket = HttpUtils.websocketRequest(httpRequest, new BellaWebSocketListener(callback));
        FlashAsrResponse response = responseConverter(future, processData);
        webSocket.close(1000, "client close");
        return response;
    }

    @Override
    public String getDescription() {
        return "火山协议";
    }

    @Override
    public Class<?> getPropertyClass() {
        return HuoshanProperty.class;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Text {
        private String text;
        private int beginTime;
        private int endTime;
    }

    private Callbacks.Sender buildSender(CompletableFuture<String> future) {
        return new Callbacks.Sender() {
            String text;
            @Override
            public void send(String text) {
                this.text = text;
            }

            @Override
            public void send(byte[] bytes) {
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
            Text obj = JacksonUtils.deserialize(text, Text.class);
            return FlashAsrResponse.builder()
                    .taskId(processData.getChannelRequestId())
                    .flashResult(FlashAsrResponse.FlashResult.builder()
                            .duration(Integer.parseInt(processData.getMetrics().get("ttlt").toString()))
                            .sentences(Lists.newArrayList(FlashAsrResponse.Sentence.builder()
                                            .beginTime(obj.getBeginTime())
                                            .endTime(obj.getEndTime())
                                            .text(obj.getText()).build()))
                            .build())
                    .user(processData.getUser())
                    .build();
        } catch (Exception e) {
            throw ChannelException.fromException(e);
        }
    }

}
