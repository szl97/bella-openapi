package com.ke.bella.openapi.endpoints;

import com.ke.bella.openapi.db.RequestInfoContext;
import com.ke.bella.openapi.protocol.AdaptorManager;
import com.ke.bella.openapi.protocol.ChannelRouter;
import com.ke.bella.openapi.protocol.IProtocolProperty;
import com.ke.bella.openapi.protocol.IProtocolAdaptor;
import com.ke.bella.openapi.protocol.completion.Callback;
import com.ke.bella.openapi.protocol.completion.CompletionRequest;
import com.ke.bella.openapi.tables.pojos.ChannelDB;
import com.ke.bella.openapi.utils.JacksonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ke.bella.openapi.annotations.EndpointAPI;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@EndpointAPI
@RestController
@RequestMapping("/v1/chat")
public class ChatController {
    @Autowired
    private ChannelRouter router;
    @Autowired
    private AdaptorManager adaptorManager;
    @PostMapping("/completions")
    public Object completion(@RequestBody CompletionRequest request) {
        String endpoint = RequestInfoContext.getRequest().getRequestURI();
        ChannelDB channel = router.route(endpoint, request.getModel(), adaptorManager.getProtocols(endpoint));
        IProtocolAdaptor.CompletionAdaptor adaptor = adaptorManager.getProtocolAdaptor(endpoint, channel.getProtocol(), IProtocolAdaptor.CompletionAdaptor.class);
        IProtocolProperty property = (IProtocolProperty) JacksonUtils.deserialize(channel.getChannelInfo(), adaptor.getPropertyClass());
        if(request.isStream()) {
            SseEmitter sseEmitter = getSseEmitter();
            adaptor.streamRequest(request, channel.getUrl(), property, new Callback.CompletionSseCallback(sseEmitter));
            return sseEmitter;
        }
        return adaptor.httpRequest(request, channel.getUrl(), property);
    }

    private static SseEmitter getSseEmitter() {
        return new SseEmitter(1000 * 60 * 60L);
    }
}
