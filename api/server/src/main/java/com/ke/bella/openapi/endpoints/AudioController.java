package com.ke.bella.openapi.endpoints;

import com.ke.bella.openapi.EndpointContext;
import com.ke.bella.openapi.protocol.audio.AudioTranscriptionRequest.*;
import com.ke.bella.openapi.protocol.audio.AudioTranscriptionResponse.*;
import com.ke.bella.openapi.service.KeQueueService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import com.ke.bella.openapi.annotations.EndpointAPI;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/v1/audio")
@Tag(name = "audio能力点")
public class AudioController {

    @Resource
    private KeQueueService keQueueService;

    @PostMapping("/transcriptions/file")
    public AudioTranscriptionResp transcribeAudio(@RequestBody AudioTranscriptionReq audioTranscriptionReq,
                                                  @RequestHeader(value = "Authorization") String authorization) {
        validateRequestParams(audioTranscriptionReq);
        String endpoint = EndpointContext.getRequest().getRequestURI();
        String taskId = keQueueService.putTask(audioTranscriptionReq, endpoint, audioTranscriptionReq.getModel(), authorization).getTaskId();
        return AudioTranscriptionResp.builder()
                .taskId(taskId)
                .build();
    }

    @PostMapping("/transcriptions/file/result")
    public AudioTranscriptionResultResp getTranscriptionResult(@RequestBody AudioTranscriptionResultReq audioTranscriptionResultReq,
                                                               @RequestHeader(value = "Authorization") String authorization) {
        List<Object> data = keQueueService.getTaskResult(audioTranscriptionResultReq.getTaskId(), authorization).getData();
        return AudioTranscriptionResultResp.builder()
                .data(data)
                .build();
    }

    private void validateRequestParams(AudioTranscriptionReq audioTranscriptionReq) {
        if (audioTranscriptionReq.getModel() == null || audioTranscriptionReq.getModel().isEmpty()) {
            throw new IllegalArgumentException("Model is required");
        }
        if (audioTranscriptionReq.getCallbackUrl() == null || audioTranscriptionReq.getCallbackUrl().isEmpty()) {
            throw new IllegalArgumentException("Callback url is required");
        }
        if (audioTranscriptionReq.getUrl() == null || audioTranscriptionReq.getUrl().isEmpty()) {
            throw new IllegalArgumentException("Url is required");
        }
        if (audioTranscriptionReq.getUser() == null || audioTranscriptionReq.getUser().isEmpty()) {
            throw new IllegalArgumentException("User is required");
        }
    }

}
