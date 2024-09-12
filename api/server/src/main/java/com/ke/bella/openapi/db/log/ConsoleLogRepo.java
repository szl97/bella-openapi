package com.ke.bella.openapi.db.log;

import com.ke.bella.openapi.EndpointProcessData;
import com.ke.bella.openapi.utils.JacksonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ConsoleLogRepo implements LogRepo {
    @Override
    public void record(EndpointProcessData log) {
        LOGGER.info(JacksonUtils.serialize(log));
    }
}
