package com.ke.bella.openapi.controller.intercet;

import com.ke.bella.openapi.controller.response.BellaResponse;
import com.ke.bella.openapi.utils.JacksonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.UnsatisfiedServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Author: Stan Sai Date: 2024/7/31 20:06 description:
 */
@RestControllerAdvice
@Slf4j
public class BellaResponseAdvice implements ResponseBodyAdvice<Object> {
    private static String stacktrace(Throwable e) {
        StringWriter writer = new StringWriter();
        e.printStackTrace(new PrintWriter(writer));
        return writer.toString();
    }

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        Class<?> clazz = returnType.getContainingClass();
        return clazz.getName().startsWith("com.ke.bella.openapi.controller");
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
            Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        response.getHeaders().add("Cache-Control", "no-cache");
        if(body instanceof BellaResponse) {
            response.setStatusCode(HttpStatus.valueOf(((BellaResponse) body).getCode()));
            return body;
        }

        BellaResponse<Object> resp = new BellaResponse<>();
        resp.setCode(200);
        resp.setTimestamp(System.currentTimeMillis());
        resp.setData(body);

        if(body instanceof String) {
            return JacksonUtils.serialize(resp);
        }
        return resp;
    }

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public BellaResponse<?> exceptionHandler(Exception e) {
        int code = 500;
        String msg = e.getLocalizedMessage();
        if(e instanceof IllegalArgumentException
                || e instanceof UnsatisfiedServletRequestParameterException
                || e instanceof MethodArgumentNotValidException) {
            code = 400;
        }

        if(code == 500) {
            LOGGER.warn(e.getMessage(), e);
        } else {
            LOGGER.info(e.getMessage());
        }

        BellaResponse<?> er = new BellaResponse<>();
        er.setCode(code);
        er.setTimestamp(System.currentTimeMillis());
        er.setMessage(msg);
        if(code == 500) {
            er.setStacktrace(stacktrace(e));
        }

        return er;
    }
}
