package com.ke.bella.openapi.controller.intercept;

import com.ke.bella.openapi.controller.EndpointRequestController;
import com.ke.bella.openapi.db.RequestInfoContext;
import com.ke.bella.openapi.protocol.ChannelException;
import com.ke.bella.openapi.protocol.OpenapiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * Author: Stan Sai Date: 2024/8/13 14:45 description:
 */
@RestControllerAdvice(assignableTypes = EndpointRequestController.class)
@Order(1)
@Slf4j
public class EndpointResponseAdvice implements ResponseBodyAdvice<Object> {
    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
            Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        response.setStatusCode(HttpStatus.OK);
        response.getHeaders().add("requestId", RequestInfoContext.getRequestId());
        return body;
    }

    @ExceptionHandler(ChannelException.class)
    @ResponseBody
    public OpenapiResponse exceptionHandler(ChannelException e) {
        OpenapiResponse.OpenapiError error = OpenapiResponse.convertFromException(e);
        OpenapiResponse openapiResponse = OpenapiResponse.errorResponse(error);
        if(e instanceof ChannelException.SafetyCheckException) {
            openapiResponse.setSensitives(((ChannelException.SafetyCheckException) e).getSensitive());
        }
        return openapiResponse;
    }

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public OpenapiResponse exceptionHandler(Exception e) {
        return exceptionHandler(ChannelException.fromException(e));
    }
}
