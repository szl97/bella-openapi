package com.ke.bella.openapi.protocol.completion;

import org.apache.commons.lang3.StringUtils;

import com.ke.bella.openapi.EndpointContext;
import com.ke.bella.openapi.TaskExecutor;
import com.ke.bella.openapi.common.exception.ChannelException;
import com.ke.bella.openapi.protocol.completion.Callbacks.StreamCompletionCallback;
import com.ke.bella.openapi.protocol.completion.CompletionResponse.Choice;
import com.ke.bella.openapi.simulation.FunctionCallContentBuffer;
import com.ke.bella.openapi.simulation.FunctionCallListener;
import com.ke.bella.openapi.simulation.SimulationHepler;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ToolCallSimulator<T extends CompletionProperty> implements CompletionAdaptor<T> {

    CompletionAdaptor<T> delegator;

    public ToolCallSimulator(CompletionAdaptor<T> adaptor) {
        this.delegator = adaptor;
    }

    @Override
    public String getDescription() {
        return delegator.getDescription();
    }

    @Override
    public Class<?> getPropertyClass() {
        return delegator.getPropertyClass();
    }

    @Override
    public CompletionResponse completion(CompletionRequest request, String url, T property) {
        if(property.isFunctionCallSimulate()) {
            CompletionRequest req = SimulationHepler.rewrite(request);
            if(req == null) {
                return delegator.completion(request, url, property);
            } else {
                EndpointContext.getProcessData().setFunctionCallSimulate(true);
                CompletionResponse resp = delegator.completion(req, url, property);
                try {
                    // 解析为function call，替换第一个choice
                    Choice choice = SimulationHepler.parse(resp.reasoning(), resp.message());
                    choice.setFinish_reason(resp.finishReason());
                    resp.getChoices().add(0, choice);
                } catch (Exception e) {
                    LOGGER.info(resp.message(), e);
                }
                return resp;
            }
        } else {
            return delegator.completion(request, url, property);
        }
    }

    @Override
    public void streamCompletion(CompletionRequest request, String url, T property, StreamCompletionCallback callback) {
        if(property.isFunctionCallSimulate()) {
            CompletionRequest req = SimulationHepler.rewrite(request);
            if(req == null) {
                delegator.streamCompletion(request, url, property, callback);
            } else {
                EndpointContext.getProcessData().setFunctionCallSimulate(true);
                Callback cb = new Callback(callback);
                delegator.streamCompletion(req, url, property, cb);
            }
        } else {
            delegator.streamCompletion(request, url, property, callback);
        }
    }

    static class Callback implements StreamCompletionCallback {
        StreamCompletionCallback sc;
        FunctionCallContentBuffer buffer;

        public Callback(StreamCompletionCallback callback) {
            this.sc = callback;
            this.buffer = new FunctionCallContentBuffer();
            TaskExecutor.submit(() -> {
                try {
                    SimulationHepler.parse(buffer, new FunctionCallListener() {
                        @Override
                        public void onMessage(StreamCompletionResponse msg) {
                            msg.setId(buffer.getLast().getId());
                            msg.setModel(buffer.getLast().getModel());
                            msg.setCreated(System.currentTimeMillis());
                            sc.callback(msg);
                        }

                        @Override
                        public void onFinish() {
                            buffer.getLasts().forEach(e -> sc.callback(e));
                            sc.done();
                            sc.finish();
                        }
                    });
                } catch (Throwable e) {
                    sc.finish(ChannelException.fromException(e));
                    e.printStackTrace();
                }
            });
        }

        @Override
        public void callback(StreamCompletionResponse msg) {
            String reasoning = msg.reasoning();
            if(StringUtils.isNotEmpty(reasoning)) {
                sc.callback(msg);
            } else {
                buffer.append(msg);
            }
        }

        @Override
        public void done() {
            buffer.finish();
        }

        @Override
        public void finish() {
            buffer.finish();
        }

        @Override
        public void finish(ChannelException exception) {
            buffer.finish();
        }
    }
}
