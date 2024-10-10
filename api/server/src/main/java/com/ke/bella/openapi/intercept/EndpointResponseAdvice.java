package com.ke.bella.openapi.intercept;

import com.ke.bella.openapi.BellaContext;
import com.ke.bella.openapi.annotations.EndpointAPI;
import com.ke.bella.openapi.protocol.ChannelException;
import com.ke.bella.openapi.protocol.OpenapiResponse;
import com.ke.bella.openapi.protocol.log.EndpointLogger;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@RestControllerAdvice(annotations = EndpointAPI.class)
@EndpointAPI
@Slf4j
public class EndpointResponseAdvice implements ResponseBodyAdvice<Object> {

    @Autowired
    private EndpointLogger logger;

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
            Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        BellaContext.getProcessData().setResponse(body);
        logger.log(BellaContext.getProcessData());
        response.getHeaders().add("X-BELLA-REQUEST-ID", BellaContext.getProcessData().getRequestId());
        return body;
    }

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public OpenapiResponse exceptionHandler(Exception exception) {
        ChannelException e = ChannelException.fromException(exception);
        OpenapiResponse.OpenapiError error = e.convertToOpenapiError();
        OpenapiResponse openapiResponse = OpenapiResponse.errorResponse(error);
        if(e instanceof ChannelException.SafetyCheckException) {
            openapiResponse.setSensitives(((ChannelException.SafetyCheckException) e).getSensitive());
        }
        if(e.getHttpCode() == 500) {
            LOGGER.error(e.getMessage(), e);
        } else {
            LOGGER.info(e.getMessage(), e);
        }
        return openapiResponse;
    }
}
