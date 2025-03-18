package com.ke.bella.openapi.utils;

import lombok.extern.slf4j.Slf4j;

import javax.servlet.AsyncContext;
import java.io.IOException;
import java.io.OutputStream;

@Slf4j
public class StreamHelper {

    public static void send(OutputStream stream, byte[] data) {
        try {
            stream.write(data);
            stream.flush();
        } catch (IOException e) {
            LOGGER.warn(e.getMessage(), e);
        }
    }

    public static void close(OutputStream stream, AsyncContext asyncContext) {
        try {
            stream.flush();
            stream.close();
            asyncContext.complete();
        } catch (IOException e) {
            LOGGER.warn(e.getMessage(), e);
        }
    }
}
