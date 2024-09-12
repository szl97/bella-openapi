package com.ke.bella.openapi.endpoints;

import com.ke.bella.openapi.BellaContext;
import com.ke.bella.openapi.EndpointProcessData;
import com.ke.bella.openapi.annotations.EndpointAPI;
import com.ke.bella.openapi.protocol.AdaptorManager;
import com.ke.bella.openapi.protocol.ChannelRouter;
import com.ke.bella.openapi.protocol.completion.Callbacks.StreamCompletionCallback;
import com.ke.bella.openapi.protocol.completion.CompletionAdaptor;
import com.ke.bella.openapi.protocol.completion.CompletionProtocolProperty;
import com.ke.bella.openapi.protocol.completion.CompletionRequest;
import com.ke.bella.openapi.protocol.log.EndpointLogger;
import com.ke.bella.openapi.tables.pojos.ChannelDB;
import com.ke.bella.openapi.utils.JacksonUtils;
import com.ke.bella.openapi.utils.SseHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@EndpointAPI
@RestController
@RequestMapping("/v1/chat")
public class ChatController {
    @Autowired
    private ChannelRouter router;
    @Autowired
    private AdaptorManager adaptorManager;
    @Autowired
    private EndpointLogger logger;

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @PostMapping("/completions")
    public Object completion(@RequestBody CompletionRequest request) {
        String endpoint = BellaContext.getRequest().getRequestURI();
        String model = request.getModel();
        ChannelDB channel = router.route(endpoint, model);
        BellaContext.setEndpointData(endpoint, model, channel, request);
        EndpointProcessData processData = BellaContext.getProcessData();
        String protocol = processData.getProtocol();
        String url = processData.getForwardUrl();
        String channelInfo = processData.getChannelInfo();
        CompletionAdaptor adaptor = adaptorManager.getProtocolAdaptor(endpoint, protocol, CompletionAdaptor.class);
        CompletionProtocolProperty property = (CompletionProtocolProperty) JacksonUtils.deserialize(channelInfo, adaptor.getPropertyClass());
        if(request.isStream()) {
            SseEmitter sse = SseHelper.createSse(1000L * 60 * 5, BellaContext.getProcessData().getRequestId());
            adaptor.streamCompletion(request, url, property, new StreamCompletionCallback(sse, processData, logger));
            return sse;
        }
        return adaptor.completion(request, url, property);
    }

}
