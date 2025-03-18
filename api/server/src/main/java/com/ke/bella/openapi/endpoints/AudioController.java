package com.ke.bella.openapi.endpoints;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ke.bella.openapi.EndpointContext;
import com.ke.bella.openapi.EndpointProcessData;
import com.ke.bella.openapi.annotations.EndpointAPI;
import com.ke.bella.openapi.protocol.AdaptorManager;
import com.ke.bella.openapi.protocol.ChannelRouter;
import com.ke.bella.openapi.protocol.asr.AudioTranscriptionRequest.AudioTranscriptionReq;
import com.ke.bella.openapi.protocol.asr.AudioTranscriptionRequest.AudioTranscriptionResultReq;
import com.ke.bella.openapi.protocol.asr.AudioTranscriptionResponse.AudioTranscriptionResp;
import com.ke.bella.openapi.protocol.asr.AudioTranscriptionResponse.AudioTranscriptionResultResp;
import com.ke.bella.openapi.protocol.limiter.LimiterManager;
import com.ke.bella.openapi.protocol.log.EndpointLogger;
import com.ke.bella.openapi.protocol.tts.TtsAdaptor;
import com.ke.bella.openapi.protocol.tts.TtsProperty;
import com.ke.bella.openapi.protocol.tts.TtsRequest;
import com.ke.bella.openapi.service.JobQueueService;
import com.ke.bella.openapi.tables.pojos.ChannelDB;
import com.ke.bella.openapi.utils.JacksonUtils;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

import static com.ke.bella.openapi.common.AudioFormat.getContentType;

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
    @Autowired
    private EndpointLogger logger;
    @Autowired
    private JobQueueService jobQueueService;


    @PostMapping("/speech")
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void speech(@RequestBody TtsRequest request, HttpServletRequest httpRequest, HttpServletResponse response) throws IOException {
        String ttsEndpoint = EndpointContext.getRequest().getRequestURI();
        String ttsModel = request.getModel();
        EndpointContext.setEndpointData(ttsEndpoint, ttsModel, request);
        EndpointProcessData processData = EndpointContext.getProcessData();
        ChannelDB ttsChannel = router.route(ttsEndpoint, ttsModel, EndpointContext.getApikey(), processData.isMock());
        EndpointContext.setEndpointData(ttsChannel);
        if(!EndpointContext.getProcessData().isPrivate()) {
            limiterManager.incrementConcurrentCount(EndpointContext.getProcessData().getAkCode(), ttsModel);
        }
        String ttsProtocol = processData.getProtocol();
        String ttsUrl = processData.getForwardUrl();
        String ttsChannelInfo = ttsChannel.getChannelInfo();

        TtsAdaptor ttsAdaptor = adaptorManager.getProtocolAdaptor(ttsEndpoint, ttsProtocol, TtsAdaptor.class);
        TtsProperty ttsProperty = (TtsProperty) JacksonUtils.deserialize(ttsChannelInfo, ttsAdaptor.getPropertyClass());
        if(StringUtils.isBlank(request.getResponseFormat())) {
            request.setResponseFormat(ttsProperty.getDefaultContentType());
        }
        EndpointContext.setEncodingType(ttsProperty.getEncodingType());
        if(request.isStream()) {
            AsyncContext asyncContext = httpRequest.startAsync();
            asyncContext.setTimeout(1200000);
            try {
                response.setContentType(getContentType(request.getResponseFormat()));
                OutputStream outputStream = response.getOutputStream();
                ttsAdaptor.streamTts(request, ttsUrl, ttsProperty,
                        ttsAdaptor.buildCallback(request, outputStream, asyncContext, processData, logger));
                return;
            } catch (Exception e) {
                asyncContext.complete();
                throw e;
            }
        }
        byte[] data = ttsAdaptor.tts(request, ttsUrl, ttsProperty);
        response.setContentType(getContentType(request.getResponseFormat()));
        response.getOutputStream().write(data);
    }


    @PostMapping("/transcriptions/file")
    public AudioTranscriptionResp transcribeAudio(@RequestBody AudioTranscriptionReq audioTranscriptionReq) {
        validateRequestParams(audioTranscriptionReq);
        String endpoint = EndpointContext.getRequest().getRequestURI();
        String taskId = jobQueueService
                .putTask(audioTranscriptionReq, endpoint, audioTranscriptionReq.getModel(), EndpointContext.getProcessData().getApikey())
                .getTaskId();
        return AudioTranscriptionResp.builder()
                .taskId(taskId)
                .build();
    }

    @PostMapping("/transcriptions/file/result")
    public AudioTranscriptionResultResp getTranscriptionResult(@RequestBody AudioTranscriptionResultReq audioTranscriptionResultReq) {
        List<Object> data = jobQueueService.getTaskResult(audioTranscriptionResultReq.getTaskId(), EndpointContext.getProcessData().getApikey()).getData();
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

