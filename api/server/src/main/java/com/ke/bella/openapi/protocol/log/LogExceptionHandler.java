package com.ke.bella.openapi.protocol.log;

import com.lmax.disruptor.ExceptionHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LogExceptionHandler implements ExceptionHandler<LogEvent> {
    @Override
    public void handleEventException(Throwable ex, long sequence, LogEvent event) {
        LOGGER.warn(ex.getMessage(), ex);
    }

    @Override
    public void handleOnStartException(Throwable ex) {
        LOGGER.warn(ex.getMessage(), ex);
    }

    @Override
    public void handleOnShutdownException(Throwable ex) {
        LOGGER.warn(ex.getMessage(), ex);
    }
}
