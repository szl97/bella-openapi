package com.ke.bella.openapi.endpoints;

import com.ke.bella.openapi.EndpointContext;
import com.ke.bella.openapi.EndpointProcessData;
import com.ke.bella.openapi.protocol.AdaptorManager;
import com.ke.bella.openapi.protocol.ChannelRouter;
import com.ke.bella.openapi.protocol.tts.TtsAdaptor;
import com.ke.bella.openapi.protocol.tts.TtsProperty;
import com.ke.bella.openapi.protocol.tts.TtsRequest;
import com.ke.bella.openapi.protocol.limiter.LimiterManager;
import com.ke.bella.openapi.tables.pojos.ChannelDB;
import com.ke.bella.openapi.utils.JacksonUtils;
import com.ke.bella.openapi.protocol.audio.AudioTranscriptionRequest.*;
import com.ke.bella.openapi.protocol.audio.AudioTranscriptionResponse.*;
import com.ke.bella.openapi.service.KeQueueService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.ke.bella.openapi.annotations.EndpointAPI;



import javax.annotation.Resource;
import java.util.List;
@EndpointAPI
@RestController
@RequestMapping("/v1/audio")
@Tag(name = "audio能力点")
@Slf4j
public class AudioController {
    @Autowired
    private ChannelRouter router;
    @Autowired
    private AdaptorManager adaptorManager;
    @Autowired
    private LimiterManager limiterManager;
    @PostMapping("/speech")
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public ResponseEntity<byte[]> speech(@RequestBody TtsRequest request) {
        String ttsEndpoint = EndpointContext.getRequest().getRequestURI();
        String ttsModel = request.getModel();
        EndpointContext.setEndpointData(ttsEndpoint, ttsModel, request);
        EndpointProcessData processData = EndpointContext.getProcessData();
        ChannelDB ttsChannel = router.route(ttsEndpoint, ttsModel, processData);
        EndpointContext.setEndpointData(ttsChannel);
        if(!EndpointContext.getProcessData().isPrivate()) {
            limiterManager.incrementConcurrentCount(EndpointContext.getProcessData().getAkCode(), ttsModel);
        }
        String ttsProtocol = processData.getProtocol();
        String ttsUrl = processData.getForwardUrl();
        String ttsChannelInfo = ttsChannel.getChannelInfo();

        TtsAdaptor ttsAdaptor = adaptorManager.getProtocolAdaptor(ttsEndpoint, ttsProtocol, TtsAdaptor.class);
        TtsProperty ttsProperty = (TtsProperty) JacksonUtils.deserialize(ttsChannelInfo, ttsAdaptor.getPropertyClass());
        EndpointContext.setEncodingType(ttsProperty.getEncodingType());
        return ttsAdaptor.tts(request, ttsUrl, ttsProperty);
    }
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

