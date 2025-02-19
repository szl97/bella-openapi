package com.ke.bella.openapi.service;

import com.ke.bella.openapi.protocol.queue.QueueRequest.*;
import com.ke.bella.openapi.protocol.queue.QueueResponse.*;
import com.ke.bella.openapi.utils.HttpUtils;
import com.ke.bella.openapi.utils.JacksonUtils;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class KeQueueService {
    @Value("${queue.put.task.url:null}")
    String putTaskUrl;
    @Value("${queue.get.task.result.url:null}")
    String getTaskResultUrl;
    @Value("${bella.file.url:null}")
    String fileUrl;

    public QueueTaskPutResp putTask(Object task, String endpoint, String model, String auth) {
        String url = putTaskUrl;
        if (url == null) {
            throw new IllegalStateException("Queue service URL 'queue.put.task.url' is not configured.");
        }
        QueueTaskPutReq req = QueueTaskPutReq.builder()
                .data(task)
                .endpoint(endpoint)
                .model(model)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", auth)
                .post(RequestBody.create(JacksonUtils.serialize(req), MediaType.parse("application/json")))
                .build();
        return HttpUtils.httpRequest(request, QueueTaskPutResp.class);
    }

    public QueueTaskGetResultResp getTaskResult(List<String> taskIds, String auth) {
        String url = getTaskResultUrl;
        List<Object> result = new ArrayList<>();
        if (url == null) {
            throw new IllegalStateException("Queue service URL 'queue.get.task.result.url' is not configured.");
        }

        for (String taskId : taskIds) {
            QueueTaskGetResultReq req = QueueTaskGetResultReq.builder()
                    .taskId(taskId)
                    .build();
            Request request = new Request.Builder().url(url)
                    .addHeader("Authorization", auth)
                    .post(RequestBody.create(JacksonUtils.serialize(req), MediaType.parse("application/json")))
                    .build();
            // todo queue batch get
            TaskGetDetailResp taskGetDetailResp = HttpUtils.httpRequest(request, TaskGetDetailResp.class);
            DetailData data = taskGetDetailResp.getData();
            if (data == null) {
                continue;
            }
            Object outputData = data.getOutputData();
            String outputFileId = data.getOutputFileId();
            if (outputData != null) {
                result.add(outputData);
            } else if (outputFileId != null && !outputFileId.isEmpty()) {
                Request requestFile = new Request.Builder()
                        .url(fileUrl + "/" + outputFileId + "/content")
                        .addHeader("Authorization", auth)
                        .get()
                        .build();
                outputData = HttpUtils.httpRequest(requestFile, Object.class);
                result.add(outputData);
            }
        }

        return QueueTaskGetResultResp.builder()
                .data(result)
                .build();
    }
}
