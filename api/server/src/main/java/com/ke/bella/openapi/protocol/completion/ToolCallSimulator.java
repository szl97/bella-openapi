package com.ke.bella.openapi.protocol.completion;

import com.ke.bella.openapi.EndpointContext;
import com.ke.bella.openapi.protocol.completion.Callbacks.StreamCompletionCallback;
import com.ke.bella.openapi.protocol.completion.CompletionResponse.Choice;
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
                    Choice choice = SimulationHepler.parse(resp.reasoning(), resp.content());
                    choice.setFinish_reason(resp.finishReason());
                    resp.getChoices().set(0, choice);
                } catch (Exception e) {
                    LOGGER.info(resp.content(), e);
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
                delegator.streamCompletion(req, url, property, callback);
            }
        } else {
            delegator.streamCompletion(request, url, property, callback);
        }
    }
}
