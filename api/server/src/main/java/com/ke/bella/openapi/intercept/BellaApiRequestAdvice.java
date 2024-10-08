package com.ke.bella.openapi.intercept;

import com.ke.bella.openapi.Operator;
import com.ke.bella.openapi.annotations.BellaAPI;
import com.ke.bella.openapi.console.ConsoleContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdviceAdapter;

import java.lang.reflect.Type;

@RestControllerAdvice(annotations = BellaAPI.class)
@Slf4j
public class BellaApiRequestAdvice extends RequestBodyAdviceAdapter {
    @Value("${spring.profiles.active}")
    private String profile;
    @Override
    public boolean supports(MethodParameter methodParameter, Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object afterBodyRead(Object body, HttpInputMessage inputMessage, MethodParameter parameter, Type targetType,
            Class<? extends HttpMessageConverter<?>> converterType) {
        if(body instanceof Operator) {
            if(profile.equals("dev") || profile.equals("ut")) {
                ConsoleContext.setOperator(ConsoleContext.SYS);
                return body;
            }
            Operator oper = (Operator) body;
            ConsoleContext.setOperator(oper);
        }
        return body;
    }
}
