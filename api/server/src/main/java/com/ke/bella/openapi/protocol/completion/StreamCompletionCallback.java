package com.ke.bella.openapi.protocol.completion;

import com.google.common.collect.Lists;
import com.ke.bella.openapi.EndpointProcessData;
import com.ke.bella.openapi.apikey.ApikeyInfo;
import com.ke.bella.openapi.common.exception.ChannelException;
import com.ke.bella.openapi.protocol.OpenapiResponse;
import com.ke.bella.openapi.protocol.log.EndpointLogger;
import com.ke.bella.openapi.safety.ISafetyCheckService;
import com.ke.bella.openapi.safety.SafetyCheckRequest;
import com.ke.bella.openapi.utils.DateTimeUtils;
import com.ke.bella.openapi.utils.PunctuationUtils;
import com.ke.bella.openapi.utils.SseHelper;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Slf4j
public class StreamCompletionCallback implements Callbacks.StreamCompletionCallback {
    public StreamCompletionCallback(SseEmitter sse, EndpointProcessData processData, ApikeyInfo apikeyInfo,
                 EndpointLogger logger, ISafetyCheckService<SafetyCheckRequest.Chat> safetyService, CompletionProperty property) {
             this.sse = sse;
             this.processData = processData;
             this.apikeyInfo = apikeyInfo;
             this.logger = logger;
             this.safetyService = safetyService;
             this.safetyCheckIndex = 0;
             this.responseBuffer = new CompletionResponse();
             responseBuffer.setCreated(DateTimeUtils.getCurrentSeconds());
             this.choiceBuffer = new HashMap<>();
             this.requestRiskData = processData.getRequestRiskData();
             this.property = property;
         }

         @Getter
         private final SseEmitter sse;
         private final EndpointProcessData processData;
         private final ApikeyInfo apikeyInfo;
         private final EndpointLogger logger;
         private final ISafetyCheckService<SafetyCheckRequest.Chat> safetyService;
         private final CompletionResponse responseBuffer;
         private final Map<Integer, CompletionResponse.Choice> choiceBuffer;
         private Integer safetyCheckIndex;
         private boolean dirtyChoice;
         private Long firstPackageTime;
         private Object requestRiskData;
         private Integer thinkStage = 0; // 推理阶段 0: 未开始; 1: 进行中; -1:结束
         private final CompletionProperty property;
         private Integer splitFlg = 0;// 拆解标志 0: 不需要; 1: 拆解中; -1:拆解结束

         @Override
         public void callback(StreamCompletionResponse msg) {
             if(requestRiskData != null) {
                 msg.setRequestRiskData(requestRiskData);
                 requestRiskData = null;
             }
             if(property.splitReasoningFromContent) {
                 splitFlg = getSplitFlg(msg, splitFlg);
                 if(splitFlg == 1 && msgContentIsNotEmpty(msg)) {
                     handleStartThink(msg);
                 } else if(splitFlg == -1) {
                     handleEndThink(msg);
                     splitFlg = 0;
                 }
             }
             if(property.mergeReasoningContent) {
                 if(CollectionUtils.isNotEmpty(msg.getChoices()) && msg.getChoices().get(0).getDelta() != null
                         && StringUtils.isNotEmpty(msg.getChoices().get(0).getDelta().getReasoning_content())) {
                     StreamCompletionResponse rebuildMsg = ResponseHelper.rebuildThinkResp(msg, thinkStage);
                     SseHelper.sendEvent(sse, rebuildMsg);
                     thinkStage = 1;
                 } else if(thinkStage == 1 && (CollectionUtils.isNotEmpty(msg.getChoices()) && msg.getChoices().get(0).getDelta() != null
                         && msg.getChoices().get(0).getDelta().getContent() != null && !Objects.equals(
                                 msg.getChoices().get(0).getDelta().getContent(), ""))) {
                     thinkStage = -1;
                     safetyCheckIndex = 0;
                     StreamCompletionResponse rebuildMsg = ResponseHelper.rebuildThinkResp(msg, thinkStage);
                     SseHelper.sendEvent(sse, rebuildMsg);
                 } else {
                     SseHelper.sendEvent(sse, msg);
                 }
             } else {
                 SseHelper.sendEvent(sse, msg);
             }
             updateBuffer(msg);
             safetyCheck(false);
         }

         private void handleStartThink(StreamCompletionResponse msg) {
             String deltaContent = msg.getChoices().get(0).getDelta().getContent().toString();
             if(deltaContent.startsWith(ResponseHelper.START_THINK)) {
                 String[] parts = deltaContent.split(ResponseHelper.START_THINK);
                 if(parts.length == 2) {
                     msg.getChoices().get(0).getDelta().setContent(parts[0]);
                     msg.getChoices().get(0).getDelta().setReasoning_content(parts[1]);
                 } else if(parts.length == 1) {
                     msg.getChoices().get(0).getDelta().setContent(parts[0]);
                 } else {
                     msg.getChoices().get(0).getDelta().setContent(StringUtils.EMPTY);
                 }
                 return;
             }
             msg.getChoices().get(0).getDelta().setContent(StringUtils.EMPTY);
             msg.getChoices().get(0).getDelta().setReasoning_content(deltaContent);
         }

         private void handleEndThink(StreamCompletionResponse msg) {
             String[] parts = msg.getChoices().get(0).getDelta().getContent().toString().split(ResponseHelper.END_THINK);
             if(parts.length == 2) {
                 msg.getChoices().get(0).getDelta().setReasoning_content(parts[0]);
                 msg.getChoices().get(0).getDelta().setContent(parts[1]);
             } else if(parts.length == 1) {
                 msg.getChoices().get(0).getDelta().setReasoning_content(parts[0]);
             } else {
                 msg.getChoices().get(0).getDelta().setContent(StringUtils.EMPTY);
             }
         }

         private Integer getSplitFlg(StreamCompletionResponse msg, Integer currentSplitFlg) {
             if(!msgContentIsNotEmpty(msg)) {
                 return currentSplitFlg;
             }
             String content = msg.getChoices().get(0).getDelta().getContent().toString();
             if(content.contains(ResponseHelper.START_THINK)) {
                 return 1;
             } else if(content.contains(ResponseHelper.END_THINK)) {
                 return -1;
             }
             return currentSplitFlg;
         }

         private boolean msgContentIsNotEmpty(StreamCompletionResponse msg) {
             return CollectionUtils.isNotEmpty(msg.getChoices()) && msg.getChoices().get(0).getDelta() != null
                     && msg.getChoices().get(0).getDelta().getContent() != null && !Objects.equals(
                             msg.getChoices().get(0).getDelta().getContent(), "");
         }

         @Override
         public void done() {
             safetyCheck(true);
             SseHelper.sendEvent(sse, "[DONE]");
         }

         public void finish() {
             sse.complete();
             log();
         }

         @Override
         public void finish(ChannelException exception) {
             OpenapiResponse.OpenapiError openapiError = exception.convertToOpenapiError();
             StreamCompletionResponse response = StreamCompletionResponse.builder()
                     .created(DateTimeUtils.getCurrentSeconds())
                     .error(openapiError)
                     .build();
             try {
                 callback(response);
             } finally {
                 sse.completeWithError(exception);
                 log();
             }
         }

         private void updateBuffer(StreamCompletionResponse streamResponse) {
             if(CollectionUtils.isEmpty(streamResponse.getChoices()) || streamResponse.getChoices().get(0).getDelta() == null) {
                 return;
             }
             if(firstPackageTime == null) {
                 firstPackageTime = streamResponse.getCreated();
             }
             ResponseHelper.overwrite(responseBuffer, streamResponse);
             StreamCompletionResponse.Choice choice = streamResponse.getChoices().get(0);
             Integer choiceIndex = choice.getIndex();
             if(!choiceBuffer.containsKey(choiceIndex)) {
                 choiceBuffer.put(choiceIndex, ResponseHelper.convert(choice));
             } else {
                 ResponseHelper.combineMessage(choiceBuffer.get(choiceIndex).getMessage(), choice.getDelta());
                 choiceBuffer.get(choiceIndex).setFinish_reason(choice.getFinish_reason());
             }
             if(isSafetyCheckChoice(choice)) {
                 dirtyChoice = true;
             }
         }

         private boolean isSafetyCheckChoice(StreamCompletionResponse.Choice choice) {
             if(choice.getIndex() != 0) {
                 return false;
             }
             return (choice.getDelta().getContent() != null && StringUtils.isNotBlank(choice.getDelta().getContent().toString()))
             || StringUtils.isNotBlank(choice.getDelta().getReasoning_content());
         }

         private void log() {
             CompletionResponse response = responseBuffer;
             long created = response.getCreated() <= 0 ? DateTimeUtils.getCurrentSeconds() : response.getCreated();
             processData.setDuration(created - processData.getRequestTime());
             processData.setFirstPackageTime(firstPackageTime == null ? DateTimeUtils.getCurrentSeconds() : firstPackageTime);
             response.setChoices(Lists.newArrayList(choiceBuffer.values()));
             processData.setResponse(response);
             logger.log(processData);
         }

         private void safetyCheck(boolean done) {
             if(!dirtyChoice) {
                 return;
             }
             CompletionResponse.Choice choice = choiceBuffer.get(0);
             if(!done) {
                 String content = choice.getMessage().getContent() == null ?
                         choice.getMessage().getReasoning_content() : choice.getMessage().getContent().toString();
                 String delta = content.substring(safetyCheckIndex);
                 if(!PunctuationUtils.endsWithPunctuation(delta)) {
                     return;
                 }
                 safetyCheckIndex = content.length();
             }
             responseBuffer.setChoices(Collections.singletonList(choice));
             Object result = safetyService.safetyCheck(SafetyCheckRequest.Chat.convertFrom(responseBuffer, processData, apikeyInfo));
             if(result != null) {
                 StreamCompletionResponse response = new StreamCompletionResponse();
                 response.setSensitives(result);
                 response.setCreated(DateTimeUtils.getCurrentSeconds());
                 SseHelper.sendEvent(sse, response);
             }
             dirtyChoice = false;
         }
}
