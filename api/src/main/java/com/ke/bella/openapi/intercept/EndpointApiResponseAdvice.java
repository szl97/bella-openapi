package com.ke.bella.openapi.intercept;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import com.ke.bella.openapi.BellaContext;
import com.ke.bella.openapi.annotations.EndpointAPI;
import com.ke.bella.openapi.protocol.ChannelException;
import com.ke.bella.openapi.protocol.OpenapiResponse;

import java.io.PrintWriter;
import java.io.StringWriter;

@RestControllerAdvice(annotations = EndpointAPI.class)
@EndpointAPI
@Slf4j
public class EndpointApiResponseAdvice implements ResponseBodyAdvice<Object> {
    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        Class<?> clazz = returnType.getContainingClass();
        return clazz.getName().startsWith("com.ke.bella.openapi.api.endpoints.")
                || clazz.isAssignableFrom(EndpointApiResponseAdvice.class);
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
            Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        response.getHeaders().add("X-BELLA-REQUEST-ID", BellaContext.getRequestId());
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
        if(e.getHttpCode() == 500) {
            LOGGER.error(e.getMessage(), e);
        } else {
            LOGGER.info(e.getMessage(), e);
        }
        return openapiResponse;
    }

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public OpenapiResponse exceptionHandler(Exception e) {
        return exceptionHandler(ChannelException.fromException(e));
    }
}
