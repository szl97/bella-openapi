package com.ke.bella.openapi.protocol;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;

import javax.servlet.ServletException;

public abstract class ChannelException extends RuntimeException {

    protected ChannelException(String message) {
        super(message);
    }

    protected ChannelException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public static ChannelException fromException(Throwable e) {
        return new ChannelException(e.getMessage(), e) {
            @Override
            public Integer getHttpCode() {
                if(e instanceof IllegalArgumentException
                        || e instanceof ServletException
                        || e instanceof MethodArgumentNotValidException) {
                    return 400;
                }
                return 500;
            }

            @Override
            public String getType() {
                if(e instanceof IllegalArgumentException) {
                    return "Illegal Argument";
                }
                return "Internal Exception";
            }
        };
    }

    /**
     * 异常对应的http状态码
     *
     * @return
     */
    public abstract Integer getHttpCode();

    /**
     * 异常code
     *
     * @return
     */
    public abstract String getType();

    public OpenapiResponse.OpenapiError convertToOpenapiError() {
        if(this instanceof ChannelException.OpenAIException) {
            return ((ChannelException.OpenAIException) this).getResponse();
        } else {
            return new OpenapiResponse.OpenapiError(this.getType(), this.getMessage(), this.getType());
        }
    }

    public static class RateLimitException extends ChannelException {
        public RateLimitException(String message) {
            super(message);
        }

        @Override
        public Integer getHttpCode() {
            return HttpStatus.TOO_MANY_REQUESTS.value();
        }

        @Override
        public String getType() {
            return HttpStatus.TOO_MANY_REQUESTS.getReasonPhrase();
        }
    }

    public static class AuthorizationException extends ChannelException {
        public AuthorizationException(String message) {
            super(message);
        }

        @Override
        public Integer getHttpCode() {
            return HttpStatus.UNAUTHORIZED.value();
        }

        @Override
        public String getType() {
            return HttpStatus.UNAUTHORIZED.getReasonPhrase();
        }
    }

    @Getter
    public static class SafetyCheckException extends ChannelException {

        protected final Integer httpCode;
        protected final String type;
        protected final Object sensitive;

        public SafetyCheckException(Integer httpCode, String type, String message, Object sensitive) {
            super(message);
            this.httpCode = httpCode;
            this.type = type;
            this.sensitive = sensitive;
        }
    }

    @Getter
    public static class OpenAIException extends ChannelException {

        protected final Integer httpCode;
        protected final String type;
        private final OpenapiResponse.OpenapiError response;

        public OpenAIException(Integer httpCode, String type, String message) {
            this(httpCode, type, message, new OpenapiResponse.OpenapiError(type, message));
        }

        public OpenAIException(Integer httpCode, String type, String message, OpenapiResponse.OpenapiError error) {
            super(message);
            this.httpCode = httpCode;
            this.type = type;
            if(error == null) {
                this.response = new OpenapiResponse.OpenapiError(type, message);
            } else {
                this.response = error;
            }
        }
    }
}
