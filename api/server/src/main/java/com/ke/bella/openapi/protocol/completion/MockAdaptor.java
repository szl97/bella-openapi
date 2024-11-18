package com.ke.bella.openapi.protocol.completion;

import com.alibaba.nacos.shaded.io.grpc.netty.shaded.io.netty.util.concurrent.DefaultThreadFactory;
import com.google.common.collect.Lists;
import com.ke.bella.openapi.BellaContext;
import com.ke.bella.openapi.common.exception.BizParamCheckException;
import com.ke.bella.openapi.utils.JacksonUtils;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Component("mock")
public class MockAdaptor implements CompletionAdaptor<CompletionProperty> {

    ExecutorService executor = new ThreadPoolExecutor(500, 500,
            0L, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(1000),
            new DefaultThreadFactory("mock-completion"));
    @Override
    public Class<?> getPropertyClass() {
        return CompletionProperty.class;
    }

    @Override
    public CompletionResponse completion(CompletionRequest request, String url, CompletionProperty property) {
        MockCompletionRequest mockCompletionRequest = buildMockRequest(BellaContext.getRequest());
        CompletionResponse response = new CompletionResponse();
        response.setCreated(Integer.parseInt(String.valueOf(System.currentTimeMillis() / 1000)));
        response.setModel("mock-model");
        response.setId("chatcmpl-" + UUID.randomUUID());
        CompletionResponse.Choice choice = new CompletionResponse.Choice();
        response.setChoices(Lists.newArrayList(choice));
        choice.setIndex(0);
        Message message = new Message();
        message.setRole("assistant");
        choice.setMessage(message);
        if(mockCompletionRequest.getFunction() == null) {
            choice.setFinish_reason("stop");
            message.setContent(mockCompletionRequest.getText());
        } else {
            choice.setFinish_reason("tool_calls");
            Message.ToolCall toolCall = new Message.ToolCall();
            toolCall.setId(UUID.randomUUID().toString());
            toolCall.setType("function");
            toolCall.setIndex(0);
            toolCall.setFunction(mockCompletionRequest.getFunction());
            message.setTool_calls(Lists.newArrayList(toolCall));
        }
        try {
            Thread.sleep(mockCompletionRequest.getTtlt());
        } catch (InterruptedException ignore) {
        }
        return response;
    }

    @Override
    public void streamCompletion(CompletionRequest request, String url, CompletionProperty property, Callbacks.StreamCompletionCallback callback) {
        MockCompletionRequest mockCompletionRequest = buildMockRequest(BellaContext.getRequest());
        StreamCompletionCallback streamCompletionCallback = (StreamCompletionCallback) callback;
        SseEmitter sseEmitter = streamCompletionCallback.getSse();
        List<String> chunks = mockCompletionRequest.getChunks();
        Runnable runnable = () -> {
            try {
                try {
                    Thread.sleep(mockCompletionRequest.getTtft());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                for (int i = 0; i < chunks.size(); i++) {
                    String chunk = chunks.get(i);
                    try {
                        sseEmitter.send(chunk);
                    } catch (IOException e) {
                        Thread.currentThread().interrupt();
                    }
                    if(i != chunks.size() - 1) {
                        try {
                            Thread.sleep(mockCompletionRequest.getInterval());
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }
                }
                try {
                    sseEmitter.send("[DONE]");
                } catch (IOException e) {
                    sseEmitter.completeWithError(e);
                }
            } finally {
                sseEmitter.complete();
            }
        };
        executor.submit(runnable);
    }

    private MockCompletionRequest buildMockRequest(HttpServletRequest request) {
        String text = request.getHeader("X-BELLA-MOCK-TEXT");
        String function = request.getHeader("X-BELLA-MOCK-FUNCTION");
        String ttft = request.getHeader("X-BELLA-MOCK-TTFT");
        String ttlt = request.getHeader("X-BELLA-MOCK-TTLT");
        String interval = request.getHeader("X-BELLA-MOCK-INTERVAL");
        MockCompletionRequest mockCompletionRequest = new MockCompletionRequest();
        try {
            if(StringUtils.isNotBlank(text)) {
                text = URLDecoder.decode(text, StandardCharsets.UTF_8.toString());
                mockCompletionRequest.setText(text);
            }
            if(StringUtils.isNotBlank(function)) {
                function = URLDecoder.decode(function, StandardCharsets.UTF_8.toString());
                mockCompletionRequest.setFunction(JacksonUtils.deserialize(function, Message.FunctionCall.class));
            }
            if(StringUtils.isNotBlank(ttft)) {
                mockCompletionRequest.setTtft(Integer.parseInt(ttft));
            }
            if(StringUtils.isNotBlank(ttlt)) {
                mockCompletionRequest.setTtlt(Integer.parseInt(ttlt));
            }
            if(StringUtils.isNotBlank(interval)) {
                mockCompletionRequest.setInterval(Integer.parseInt(interval));
            }
        } catch (UnsupportedEncodingException e) {
            throw new BizParamCheckException(e.getMessage());
        }
        if(StringUtils.isBlank(mockCompletionRequest.getText()) && mockCompletionRequest.getFunction() == null) {
            throw new BizParamCheckException("参数非法");
        }
        return mockCompletionRequest;
    }

    @Data
    public static class MockCompletionRequest {
        private String text;
        private Message.FunctionCall function;
        private Integer ttft = 500;
        private Integer ttlt = 60 * 1000;
        private Integer interval = 1000;

        private int packageSize() {
            if(text != null) {
                return Math.max(1, text.length() / totalPackets());
            } else {
                return Math.max(1, function.getArguments().length() / totalPackets());
            }
        }

        private int totalPackets() {
            return Math.max((ttlt - ttft) / interval, 1);
        }

        private int getLen() {
            if(text != null) {
                return text.length();
            } else {
                return function.getArguments().length();
            }
        }

        private String chunk(int start, int end, boolean isLast) {
            StreamCompletionResponse response = new StreamCompletionResponse();
            response.setId("chatcmpl-" + UUID.randomUUID());
            response.setModel("mock-model");
            response.setCreated(Integer.parseInt(String.valueOf(System.currentTimeMillis() / 1000)));
            StreamCompletionResponse.Choice choice = new StreamCompletionResponse.Choice();
            response.setChoices(Lists.newArrayList(choice));
            choice.setIndex(0);
            Message message = new Message();
            message.setRole("assistant");
            choice.setDelta(message);
            if(text != null) {
                String chunk = text.substring(start, end);
                message.setContent(chunk);
                if(isLast) {
                    choice.setFinish_reason("stop");
                }
            } else {
                Message.ToolCall toolCall = new Message.ToolCall();
                toolCall.setId(UUID.randomUUID().toString());
                toolCall.setType("function");
                toolCall.setIndex(0);
                Message.FunctionCall function = new Message.FunctionCall();
                toolCall.setFunction(function);
                if(start == 0) {
                    function.setName(this.function.getName());
                }
                function.setArguments(this.function.getArguments().substring(start, end));
                message.setTool_calls(Lists.newArrayList(toolCall));
                if(isLast) {
                    choice.setFinish_reason("tool_calls");
                }
            }
            return JacksonUtils.serialize(response);
        }

        public List<String> getChunks() {
            int charsPerPacket = packageSize();
            int length = getLen();
            List<String> chunks = new ArrayList<>();
            for (int i = 0; i < length; i += charsPerPacket) {
                int end = Math.min(i + charsPerPacket, length);
                chunks.add(chunk(i, end, end == length));
            }
            return chunks;
        }
    }
}
