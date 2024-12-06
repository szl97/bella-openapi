package com.ke.bella.openapi.endpoints;

import com.ke.bella.openapi.BellaContext;
import com.ke.bella.openapi.EndpointProcessData;
import com.ke.bella.openapi.annotations.EndpointAPI;
import com.ke.bella.openapi.protocol.AdaptorManager;
import com.ke.bella.openapi.protocol.ChannelRouter;
import com.ke.bella.openapi.protocol.embedding.EmbeddingAdaptor;
import com.ke.bella.openapi.protocol.embedding.EmbeddingProperty;
import com.ke.bella.openapi.protocol.embedding.EmbeddingRequest;
import com.ke.bella.openapi.tables.pojos.ChannelDB;
import com.ke.bella.openapi.utils.JacksonUtils;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@EndpointAPI
@RestController
@RequestMapping("/v1/embeddings")
@Tag(name = "chat")
public class EmbeddingController {
    @Autowired
    private ChannelRouter router;
    @Autowired
    private AdaptorManager adaptorManager;
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @PostMapping
    public Object embedding(@RequestBody EmbeddingRequest request) {
        String endpoint = BellaContext.getRequest().getRequestURI();
        String model = request.getModel();
        BellaContext.setEndpointData(endpoint, model, request);
        boolean isMock = BellaContext.getProcessData().isMock();
        ChannelDB channel = router.route(endpoint, model, isMock);
        BellaContext.setEndpointData(channel);
        EndpointProcessData processData = BellaContext.getProcessData();
        String protocol = processData.getProtocol();
        String url = processData.getForwardUrl();
        String channelInfo = channel.getChannelInfo();
        EmbeddingAdaptor adaptor = adaptorManager.getProtocolAdaptor(endpoint, protocol, EmbeddingAdaptor.class);
        EmbeddingProperty property = (EmbeddingProperty) JacksonUtils.deserialize(channelInfo, adaptor.getPropertyClass());
        BellaContext.setEncodingType(property.getEncodingType());
        return adaptor.embedding(request, url, property);
    }
}
