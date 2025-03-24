package com.ke.bella.openapi.protocol.asr;

import com.ke.bella.openapi.EndpointProcessData;
import com.ke.bella.openapi.common.exception.ChannelException;
import com.ke.bella.openapi.protocol.Callbacks;
import com.ke.bella.openapi.protocol.asr.realtime.AsrEventType;
import com.ke.bella.openapi.protocol.asr.realtime.RealTimeAsrMessage;
import com.ke.bella.openapi.protocol.log.EndpointLogger;
import com.ke.bella.openapi.utils.DateTimeUtils;
import com.ke.bella.openapi.utils.JacksonUtils;

import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;
import okhttp3.WebSocket;
import okio.ByteString;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.ke.bella.openapi.protocol.asr.realtime.AsrEventType.TRANSCRIPTION_STARTED;

@Slf4j
public class KeStreamAsrCallback implements Callbacks.WebSocketCallback {

    private final TextSender sender;
    private final EndpointProcessData processData;
    private final EndpointLogger logger;
    private final String taskId;

    private final CompletableFuture<?> startFlag = new CompletableFuture<>();

    private final long startTime = DateTimeUtils.getCurrentMills();
    private boolean end = false;
    private boolean first = true;

    public KeStreamAsrCallback(TextSender sender, EndpointProcessData processData, EndpointLogger logger, String taskId) {
        this.sender = sender;
        this.processData = processData;
        this.logger = logger;
        this.taskId = taskId;
    }

    @Override
    public void onOpen(WebSocket webSocket, Response response) {
        processData.setChannelRequestId(taskId);
    }

    @Override
    public void onMessage(WebSocket webSocket, ByteString bytes) {
    }

    @Override
    public void onMessage(WebSocket webSocket, String text) {
        
        RealTimeAsrMessage message = JacksonUtils.deserialize(text, RealTimeAsrMessage.class);
        if(message == null || message.getHeader() == null || message.getHeader().getName() == null) {
            LOGGER.warn("无效的ASR响应消息格式:{}", text);
            return;
        }

        String eventName = message.getHeader().getName();
        AsrEventType eventType = AsrEventType.fromString(eventName);

        if(eventType != TRANSCRIPTION_STARTED) {
            sender.send(text);
        }

        switch (eventType) {
            case TRANSCRIPTION_STARTED:
                startFlag.complete(null);
                break;

            case TRANSCRIPTION_COMPLETED:
                complete();
                break;
                
            case TRANSCRIPTION_FAILED:
            case TASK_FAILED:
                LOGGER.warn("转录失败: {}",
                    message.getHeader().getStatus_message() != null ? 
                    message.getHeader().getStatus_message() : "未知原因");
                complete();
                break;
                
            case UNKNOWN:
                LOGGER.warn("收到未知事件类型: {}, text:{}", eventName, text);
                break;
        }
    }

    @Override
    public void onClosing(WebSocket webSocket, int code, String reason) {
        webSocket.close(1000, "Client closing");
    }

    @Override
    public void onClosed(WebSocket webSocket, int code, String reason) {
        LOGGER.info("ASR onClosed: code={}, reason={}", code, reason);
        complete();
    }

    @Override
    public void onFailure(WebSocket webSocket, Throwable t, Response response) {
        if(t != null){
            onError(ChannelException.fromException(t));
        } else {
            onError(ChannelException.fromResponse(response.code(), response.message()));
        }
    }

    @Override
    public boolean started() {
        try {
            startFlag.get(30, TimeUnit.SECONDS);
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw ChannelException.fromException(e);
        } catch (ExecutionException | TimeoutException e) {
            throw ChannelException.fromException(e);
        }
    }

    private void onError(ChannelException exception) {
        LOGGER.warn("ASR error: {}", exception.getMessage(), exception);
        sender.onError(exception);
        complete();
    }

    private void complete() {
        if (!end) {
            sender.close();
            processData.getMetrics().put("ttlt", DateTimeUtils.getCurrentMills() - startTime);
            logger.log(processData);
            end = true;
        }
    }
}
