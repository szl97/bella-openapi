package com.ke.bella.openapi.utils;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import com.fasterxml.jackson.core.type.TypeReference;
import com.ke.bella.openapi.common.exception.ChannelException;
import com.ke.bella.openapi.protocol.BellaEventSourceListener;
import com.ke.bella.openapi.protocol.BellaWebSocketListener;
import com.ke.bella.openapi.protocol.completion.Callbacks;

import okhttp3.Callback;
import okhttp3.ConnectionPool;
import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.internal.Util;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSources;

/**
 * Author: Stan Sai Date: 2024/8/14 12:09 description:
 */
public class HttpUtils {
    private static final ConnectionPool connectionPool = new ConnectionPool(1500, 5, TimeUnit.MINUTES);
    private static final ExecutorService executorService = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 60, TimeUnit.SECONDS,
            new SynchronousQueue<>(), Util.threadFactory("OkHttp Dispatcher", false));
    private static final OkHttpClient client = okhttpClient();
    public static EventSource.Factory factory = EventSources.createFactory(client);
    private static OkHttpClient okhttpClient() {
        Dispatcher dispatcher = new Dispatcher(executorService);
        dispatcher.setMaxRequests(2000);
        dispatcher.setMaxRequestsPerHost(500);
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectionPool(connectionPool)
                .dispatcher(dispatcher)
                .pingInterval(50, TimeUnit.SECONDS)
                .connectTimeout(2, TimeUnit.MINUTES)
                .readTimeout(5, TimeUnit.MINUTES);
        return builder.build();
    }

    public static Response httpRequest(Request request) throws IOException {
        return client.newCall(request).execute();
    }

    public static void streamRequest(Request request, Callback callback) {
        client.newCall(request).enqueue(callback);
    }

    public static <T> T httpRequest(Request request, Class<T> clazz) {
        return doHttpRequest(request, bytes -> JacksonUtils.deserialize(bytes, clazz), null);
    }

    public static <T> T httpRequest(Request request, TypeReference<T> typeReference) {
        return doHttpRequest(request, bytes -> JacksonUtils.deserialize(bytes, typeReference), null);
    }

    public static <T> T httpRequest(Request request, Class<T> clazz, Callbacks.ChannelErrorCallback<T> errorCallback) {
        return doHttpRequest(request, bytes -> JacksonUtils.deserialize(bytes, clazz), errorCallback);
    }

    public static <T> T httpRequest(Request request, TypeReference<T> typeReference, Callbacks.ChannelErrorCallback<T> errorCallback) {
        return doHttpRequest(request, bytes -> JacksonUtils.deserialize(bytes, typeReference), errorCallback);
    }

    private static  <T> T doHttpRequest(Request request, Function<byte[], T> responseConvert, Callbacks.ChannelErrorCallback<T> errorCallback) {
        T result = null;
        try {
            Response response = HttpUtils.httpRequest(request);
            if(response.body() != null) {
                result = responseConvert.apply(response.body().bytes());
            }
            if(response.code() > 299) {
                if(result == null) {
                    if(response.code() > 499 && response.code() < 600) {
                        String message = "供应商返回：code: " + response.code() + " message: " + response.message();
                        throw ChannelException.fromResponse(503, message);
                    }
                    throw ChannelException.fromResponse(response.code(), response.message());
                } else {
                    if(errorCallback != null) {
                        errorCallback.callback(result, response);
                    }
                }
            }
            return result;
        } catch (IOException e) {
            throw ChannelException.fromException(e);
        }
    }

    public static byte[] doHttpRequest(Request request) {
        Response response = null;
        ResponseBody body = null;
        try {
            response = HttpUtils.httpRequest(request);
            body = response.body();

            byte[] bodyBytes = null;
            if(body != null) {
                bodyBytes = body.bytes();
            }

            if(!response.isSuccessful()) {
                throw new IllegalStateException(String.format("failed to do http request, code: %s, message: %s ",
                        response.code(),
                        Optional.ofNullable(bodyBytes).map(String::new).orElse(null)));
            } else {
                return bodyBytes;
            }

        } catch (IOException e) {
            throw new IllegalStateException(e);
        } finally {
            if(response != null) {
                response.close();
            }
            if(body != null) {
                body.close();
            }
        }
    }

    /**
     * 当且仅当http code为2xx时进行反序列化
     *
     * @param request
     * @param reference
     *
     * @return
     *
     * @param <T>
     */
    public static <T> T doHttpRequest(Request request, TypeReference<T> reference) {
        Response response = null;
        ResponseBody body = null;
        try {
            response = HttpUtils.httpRequest(request);
            body = response.body();

            String bodyStr = null;
            if(body != null) {
                bodyStr = body.string();
            }

            if(!response.isSuccessful()) {
                throw new IllegalStateException(String.format("failed to do http request, code: %s, message: %s ",
                        response.code(),
                        Optional.ofNullable(bodyStr).map(String::new).orElse(null)));
            } else {
                return JacksonUtils.deserialize(bodyStr, reference);
            }

        } catch (IOException e) {
            throw new IllegalStateException(e);
        } finally {
            if(response != null) {
                response.close();
            }
            if(body != null) {
                body.close();
            }
        }
    }

    public static void streamRequest(Request request, BellaEventSourceListener listener) {
        CompletableFuture<?> future = new CompletableFuture<>();
        listener.setConnectionInitFuture(future);
        factory.newEventSource(request, listener);
        try {
             future.get();
        } catch (InterruptedException interruptedException) {
            interruptedException.printStackTrace();
            Thread.currentThread().interrupt();
        }  catch (ExecutionException e) {
            if(e.getCause() instanceof RuntimeException) {
                throw (RuntimeException) e.getCause();
            }
            throw new RuntimeException(e);
        }
    }

    public static void websocketRequest(Request request, BellaWebSocketListener listener) {
        CompletableFuture<?> future = new CompletableFuture<>();
        listener.setConnectionInitFuture(future);
        client.newWebSocket(request, listener);
        try {
            future.get();
        } catch (InterruptedException interruptedException) {
            interruptedException.printStackTrace();
            Thread.currentThread().interrupt();
        }  catch (ExecutionException e) {
            if(e.getCause() instanceof RuntimeException) {
                throw (RuntimeException) e.getCause();
            }
            throw new RuntimeException(e);
        }
    }
}
