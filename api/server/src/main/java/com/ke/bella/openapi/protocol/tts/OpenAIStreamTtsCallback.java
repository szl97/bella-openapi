package com.ke.bella.openapi.protocol.tts;

import java.io.OutputStream;
import java.util.HashMap;

import javax.servlet.AsyncContext;

import com.ke.bella.openapi.EndpointProcessData;
import com.ke.bella.openapi.common.exception.ChannelException;
import com.ke.bella.openapi.protocol.Callbacks;
import com.ke.bella.openapi.protocol.OpenapiResponse;
import com.ke.bella.openapi.protocol.log.EndpointLogger;
import com.ke.bella.openapi.utils.DateTimeUtils;
import com.ke.bella.openapi.utils.StreamHelper;

public class OpenAIStreamTtsCallback implements Callbacks.HttpStreamTtsCallback {

    final OutputStream stream;
    final AsyncContext context;
    final EndpointProcessData processData;
    final EndpointLogger logger;

    boolean first = true;
    long startTime = DateTimeUtils.getCurrentMills();

    public OpenAIStreamTtsCallback(OutputStream stream, AsyncContext context, EndpointProcessData processData, EndpointLogger logger) {
        this.stream = stream;
        this.context = context;
        this.processData = processData;
        this.logger = logger;
        processData.setMetrics(new HashMap<>());
    }

    @Override
    public void onOpen() {

    }

    @Override
    public void callback(byte[] msg) {
        StreamHelper.send(stream, msg);
        if(first) {
            processData.getMetrics().put("ttft", DateTimeUtils.getCurrentMills() - startTime);
            first = false;
        }
    }

    @Override
    public void finish() {
        processData.getMetrics().put("ttlt", DateTimeUtils.getCurrentMills() - startTime);
        StreamHelper.close(stream, context);
        logger.log(processData);
    }

    @Override
    public void finish(ChannelException exception) {
        processData.setResponse(OpenapiResponse.errorResponse(exception.convertToOpenapiError()));
        finish();
    }
}
