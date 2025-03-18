package com.ke.bella.openapi.protocol.tts;

import java.io.IOException;

import com.ke.bella.openapi.common.exception.ChannelException;
import com.ke.bella.openapi.protocol.BellaStreamCallback;
import com.ke.bella.openapi.protocol.Callbacks;

import lombok.extern.slf4j.Slf4j;
import okhttp3.Call;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.BufferedSource;

@Slf4j
public class TtsStreamListener extends BellaStreamCallback {
    private final Callbacks.HttpStreamTtsCallback callback;

    public TtsStreamListener(Callbacks.HttpStreamTtsCallback callback) {
        this.callback = callback;
    }

    @Override
    protected void onOpen() {
        super.onOpen();
        callback.onOpen();
    }

    @Override
    public void onFailure(Call call, IOException e) {
        LOGGER.error("TTS流式请求失败", e);
        ChannelException exception = ChannelException.fromException(e);
        if(!connectionInitFuture.isDone()) {
            connectionInitFuture.completeExceptionally(exception);
        } else {
            callback.finish(exception);
        }
    }

    @Override
    public void onResponse(Call call, Response response) {
        if(!response.isSuccessful()) {
            String errorMsg = "TTS流式请求返回错误状态码: " + response.code() + ", message: " + response.message();
            LOGGER.error(errorMsg);
            ChannelException exception = ChannelException.fromResponse(response.code(), response.message());
            if(connectionInitFuture.isDone()) {
                callback.finish(exception);
            } else {
                connectionInitFuture.completeExceptionally(exception);
            }
            return;
        }

        onOpen();

        ResponseBody body = response.body();
        if(body == null) {
            LOGGER.warn("TTS流式响应体为空");
            callback.finish();
            return;
        }
        try {
            byte[] buffer = new byte[8192];
            try (BufferedSource source = body.source()) {
                int bytesRead;
                while ((bytesRead = source.read(buffer)) != -1) {
                    if (bytesRead > 0) {
                        byte[] audioData = new byte[bytesRead];
                        System.arraycopy(buffer, 0, audioData, 0, bytesRead);
                        callback.callback(audioData);
                    }
                }
                callback.finish();
            }
        } catch (IOException e) {
            LOGGER.error("读取TTS流式数据失败", e);
            ChannelException exception = ChannelException.fromException(e);
            callback.finish(exception);
        } finally {
            if (body != null) {
                body.close();
            }
        }
    }
}
