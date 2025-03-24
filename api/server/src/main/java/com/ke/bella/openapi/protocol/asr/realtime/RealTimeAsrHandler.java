package com.ke.bella.openapi.protocol.asr.realtime;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.ke.bella.openapi.EndpointProcessData;
import com.ke.bella.openapi.common.exception.ChannelException;
import com.ke.bella.openapi.protocol.Callbacks;
import com.ke.bella.openapi.protocol.Callbacks.WebSocketCallback;
import com.ke.bella.openapi.protocol.asr.AsrProperty;
import com.ke.bella.openapi.protocol.log.EndpointLogger;
import com.ke.bella.openapi.utils.JacksonUtils;

import okhttp3.WebSocket;

/**
 * 实时语音识别WebSocket处理器
 */

public class RealTimeAsrHandler extends TextWebSocketHandler {

    private final String url;
    private final AsrProperty property;
    private final EndpointProcessData processData;
    private final EndpointLogger logger;
    private final RealTimeAsrAdaptor<AsrProperty> adaptor;
    private static final Logger LOGGER = LoggerFactory.getLogger(RealTimeAsrHandler.class);
    private String taskId;
    // 与ASR服务的WebSocket连接
    private WebSocket asrWebSocket;
    private WebSocketCallback callback;

    public RealTimeAsrHandler(String url, AsrProperty property, EndpointProcessData processData, EndpointLogger logger, RealTimeAsrAdaptor<AsrProperty> adaptor) {
        this.url = url;
        this.property = property;
        this.processData = processData;
        this.logger = logger;
        this.adaptor = adaptor;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        LOGGER.info("客户端WebSocket连接已建立: {}", session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            String payload = message.getPayload();
            
            // 先解析基本消息结构，获取消息类型
            RealTimeAsrMessage asrMessage = JacksonUtils.deserialize(payload, RealTimeAsrMessage.class);
            if (asrMessage == null || asrMessage.getHeader() == null || asrMessage.getHeader().getName() == null) {
                sendErrorResponse(session, 40000000,"无效的请求格式");
                return;
            }
            
            String eventName = asrMessage.getHeader().getName();
            switch (eventName) {
                case "StartTranscription":
                    handleStartTranscription(session, asrMessage);
                    break;
                case "StopTranscription":
                    handleStopTranscription(session, asrMessage);
                    break;
                default:
                    sendErrorResponse(session, 40000000,"不支持的事件类型: " + eventName);
                    break;
            }
        } catch (Exception e) {
            LOGGER.warn("处理文本消息时出错: {}", e.getMessage());
            sendErrorResponse(session, 50000000,"处理请求时出错: " + e.getMessage());
        }
    }
    
    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) {
        try {
            byte[] audioData = message.getPayload().array();
            
            if (taskId == null) {
                sendErrorResponse(session, 40000000,"未开始转录任务，请先发送StartTranscription指令");
                return;
            }
            
            if (asrWebSocket == null) {
                sendErrorResponse(session, 50000000,"未连接到ASR服务");
                return;
            }
            
            // 发送音频数据到第三方ASR服务
            boolean success = adaptor.sendAudioData(asrWebSocket, audioData, callback);
            if (!success) {
                sendErrorResponse(session, 50000000,"发送音频数据失败");
            }
            
        } catch (Exception e) {
            LOGGER.warn("处理二进制消息时出错: {}", e.getMessage());
            sendErrorResponse(session, 50000000,"处理音频数据时出错: " + e.getMessage());
        }
    }
    
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        LOGGER.info("客户端WebSocket连接已关闭, status: {}", status);
        
        // 关闭与第三方ASR服务的连接
        if (asrWebSocket != null) {
            adaptor.closeConnection(asrWebSocket);
            asrWebSocket = null;
        }
        
        taskId = null;
    }
    
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        LOGGER.warn("WebSocket传输错误: {}", exception.getMessage());
        
        // 关闭与第三方ASR服务的连接
        if (asrWebSocket != null) {
            adaptor.closeConnection(asrWebSocket);
            asrWebSocket = null;
        }
        
        taskId = null;
    }

    private void handleStartTranscription(WebSocketSession session, RealTimeAsrMessage request) throws IOException {
        if (taskId != null) {
            sendErrorResponse(session, 40000000,"已有转录任务正在进行");
            return;
        }
        
        // 获取或生成任务ID
        taskId = request.getHeader().getTask_id();
        if (taskId == null) {
            taskId = UUID.randomUUID().toString();
        }
        
        // 创建回调处理器
        callback = adaptor.createCallback(webSocketSender(session, processData), processData, logger, taskId, request, property);
        
        // 创建与第三方ASR服务的连接并开始转录
        asrWebSocket = adaptor.startTranscription(url, property, request, callback);
        
        if (asrWebSocket == null) {
            taskId = null;
            sendErrorResponse(session, 50000000,"无法连接到ASR服务");
            return;
        }

        // 发送TranscriptionStarted响应
        sendTranscriptionStartedResponse(session, taskId);
    }
    
    private void handleStopTranscription(WebSocketSession session, RealTimeAsrMessage request) {
        if (taskId == null || asrWebSocket == null) {
            sendErrorResponse(session, 40000000, "没有正在进行的转录任务");
            return;
        }
        
        String msgTaskId = request.getHeader().getTask_id();
        if (msgTaskId != null && !msgTaskId.equals(taskId)) {
            sendErrorResponse(session, 40000000, "无效的任务ID");
            return;
        }
        
        // 发送结束转录指令
        boolean success = adaptor.stopTranscription(asrWebSocket, request, callback);
        
        if (!success) {
            sendErrorResponse(session, 50000000,"无法停止转录任务");
        }
    }

    private void sendTranscriptionStartedResponse(WebSocketSession session, String taskId) throws IOException {
        RealTimeAsrMessage response = RealTimeAsrMessage.startedResponse(taskId);
        session.sendMessage(new TextMessage(JacksonUtils.serialize(response)));
    }

    private RealTimeAsrMessage sendErrorResponse(WebSocketSession session, int status, String errorMessage) {
        int httpCode =  status >= 50000000 ? 500 : 400;
        RealTimeAsrMessage response = RealTimeAsrMessage.errorResponse(httpCode, status, errorMessage, taskId);
        try {
            session.sendMessage(new TextMessage(JacksonUtils.serialize(response)));
        } catch (IOException e) {
            LOGGER.warn("发送错误响应失败: {}", e.getMessage());
        }
        return response;
    }

    private Callbacks.TextSender webSocketSender(WebSocketSession session, EndpointProcessData processData) {
        final AtomicInteger duration = new AtomicInteger(0);
        return new Callbacks.TextSender() {
            @Override
            public void send(String text) {
                try {
                    session.sendMessage(new TextMessage(text));
                } catch (IOException e) {
                    LOGGER.warn(e.getMessage(), e);
                }
                RealTimeAsrMessage message = JacksonUtils.deserialize(text, RealTimeAsrMessage.class);
                if(message.getHeader().getName().equals(AsrEventType.SENTENCE_END.getValue())) {
                    int time = (int) Math.ceil((message.getPayload().getTime() - message.getPayload().getBegin_time()) / 1000.0);
                    duration.getAndAdd(time);
                }
            }

            @Override
            public void onError(Throwable e) {
                ChannelException exception = ChannelException.fromException(e);
                RealTimeAsrMessage res = sendErrorResponse(session, exception.getHttpCode() < 500 ? 40000000 : 50000000, exception.getMessage());
                processData.setResponse(res);
            }

            @Override
            public void close() {
                try {
                    processData.setDuration(duration.get());
                    session.close();
                } catch (IOException e) {
                    LOGGER.warn(e.getMessage(), e);
                }
            }
        };
    }
}
