package com.ke.bella.openapi.protocol.asr.realtime;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.ke.bella.openapi.EndpointProcessData;
import com.ke.bella.openapi.protocol.BellaWebSocketListener;
import com.ke.bella.openapi.protocol.Callbacks;
import com.ke.bella.openapi.protocol.asr.HuoshanProperty;
import com.ke.bella.openapi.protocol.asr.HuoshanRealTimeAsrRequest;
import com.ke.bella.openapi.protocol.asr.HuoshanRealTimeAsrResponse;
import com.ke.bella.openapi.protocol.asr.HuoshanStreamAsrCallback;
import com.ke.bella.openapi.protocol.log.EndpointLogger;
import com.ke.bella.openapi.utils.HttpUtils;
import com.ke.bella.openapi.utils.JacksonUtils;

import okhttp3.Request;
import okhttp3.WebSocket;

@Component("HuoshanRealtimeAsr")
public class HuoshanAdpaptor implements RealTimeAsrAdaptor<HuoshanProperty> {

    @Override
    public WebSocket startTranscription(String url, HuoshanProperty property, RealTimeAsrMessage request, Callbacks.WebSocketCallback callback) {
        Request.Builder builder = authorizationRequestBuilder(property.getAuth());
        builder.url(url);
        WebSocket webSocket = HttpUtils.websocketRequest(builder.build(), new BellaWebSocketListener(callback));
        webSocket.send(JacksonUtils.serialize(request));
        callback.started();
        return webSocket;
    }

    @Override
    public boolean sendAudioData(WebSocket webSocket, byte[] audioData, Callbacks.WebSocketCallback callback) {
        HuoshanStreamAsrCallback huoshanCallback = (HuoshanStreamAsrCallback) callback;
        huoshanCallback.sendAudioData(webSocket, audioData, false);
        return true;
    }

    @Override
    public boolean stopTranscription(WebSocket webSocket, RealTimeAsrMessage request, Callbacks.WebSocketCallback callback) {
        HuoshanStreamAsrCallback huoshanCallback = (HuoshanStreamAsrCallback) callback;
        huoshanCallback.sendAudioData(webSocket, null, true);
        return true;
    }

    @Override
    public void closeConnection(WebSocket webSocket) {
        webSocket.close(1000, "client close");
    }

    @Override
    public Callbacks.WebSocketCallback createCallback(Callbacks.TextSender sender, EndpointProcessData processData, EndpointLogger logger,
            String taskId, RealTimeAsrMessage request, HuoshanProperty property) {
        HuoshanRealTimeAsrRequest huoshanRealTimeAsrRequest = new HuoshanRealTimeAsrRequest(request, property);
        return new HuoshanStreamAsrCallback(huoshanRealTimeAsrRequest, sender, processData, logger, new converter(taskId));
    }

    @Override
    public String getDescription() {
        return "火山协议";
    }

    @Override
    public Class<HuoshanProperty> getPropertyClass() {
        return HuoshanProperty.class;
    }

    public static class converter implements Function<HuoshanRealTimeAsrResponse, List<String>> {

        private final String taskId;

        boolean sentenceStart = true;

        int index = -1;

        public converter(String taskId) {
            this.taskId = taskId;
        }

        @Override
        public List<String> apply(HuoshanRealTimeAsrResponse response) {
            List<String> result = new ArrayList<>();
            if(sentenceStart) {
                index++;
                RealTimeAsrMessage.Payload payload = new RealTimeAsrMessage.Payload();
                payload.setIndex(index);
                payload.setTime(response.getAddition() != null ? response.getAddition().getDuration() : 0);
                RealTimeAsrMessage start = RealTimeAsrMessage.sentenceBegin(taskId, payload);
                result.add(JacksonUtils.serialize(start));
                sentenceStart = false;
            }
            if(response.getResult() != null) {
                for(HuoshanRealTimeAsrResponse.Result tempResult : response.getResult()) {
                    if(tempResult.getUtterances() == null) {
                        continue;
                    }
                    for(HuoshanRealTimeAsrResponse.Result huoshanResult : tempResult.getUtterances()) {
                        RealTimeAsrMessage.Payload payload = new RealTimeAsrMessage.Payload();
                        payload.setIndex(index);
                        payload.setTime(huoshanResult.getTime());
                        payload.setResult(huoshanResult.getText());
                        payload.setConfidence(huoshanResult.getConfidence());
                        if(huoshanResult.getWords() != null) {
                            payload.setWords(huoshanResult.getWords().stream().map(HuoshanRealTimeAsrResponse.Result.Word::convert)
                                    .collect(Collectors.toList()));
                        }
                        if(huoshanResult.isDefinite()) {
                            payload.setBegin_time(huoshanResult.getBegin_time());
                            RealTimeAsrMessage end = RealTimeAsrMessage.SentenceEnd(taskId, payload);
                            result.add(JacksonUtils.serialize(end));
                            sentenceStart = true;
                        } else {
                            RealTimeAsrMessage changed = RealTimeAsrMessage.resultChange(taskId, payload);
                            result.add(JacksonUtils.serialize(changed));
                        }
                    }
                }
            }
            if(response.getSequence() < 0) {
                RealTimeAsrMessage completion = RealTimeAsrMessage.completion(taskId);
                result.add(JacksonUtils.serialize(completion));
            }
            return result;
        }
    }
}
