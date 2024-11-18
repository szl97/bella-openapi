package com.ke.bella.openapi.protocol.completion;

import com.google.common.collect.Lists;
import com.ke.bella.openapi.EndpointProcessData;
import com.ke.bella.openapi.apikey.ApikeyInfo;
import com.ke.bella.openapi.exception.ChannelException;
import com.ke.bella.openapi.protocol.OpenapiResponse;
import com.ke.bella.openapi.protocol.log.EndpointLogger;
import com.ke.bella.openapi.safety.ISafetyCheckService;
import com.ke.bella.openapi.safety.SafetyCheckRequest;
import com.ke.bella.openapi.utils.DateTimeUtils;
import com.ke.bella.openapi.utils.PunctuationUtils;
import com.ke.bella.openapi.utils.SseHelper;
import lombok.Getter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public interface Callbacks {

     @FunctionalInterface
     interface SseEventConverter<T> {
         T convert(String id, String event, String msg);
     }
     class StreamCompletionCallback {
         public StreamCompletionCallback(SseEmitter sse, EndpointProcessData processData, ApikeyInfo apikeyInfo,
                 EndpointLogger logger, ISafetyCheckService<SafetyCheckRequest.Chat> safetyService) {
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

         public void callback(StreamCompletionResponse msg) {
             if(requestRiskData != null) {
                 msg.setRequestRiskData(requestRiskData);
                 requestRiskData = null;
             }
             SseHelper.sendEvent(sse, msg);
             updateBuffer(msg);
             safetyCheck(false);
         }

         public void done() {
             safetyCheck(true);
             SseHelper.sendEvent(sse, "[DONE]");
         }

         public void finish() {
             sse.complete();
             log();
         }

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
             return choice.getDelta().getContent() != null && StringUtils.isNotBlank(choice.getDelta().getContent().toString());
         }

         private void log() {
             CompletionResponse response = responseBuffer;
             processData.setDuration(response.getCreated() - processData.getRequestTime());
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
                 String content = choice.getMessage().getContent().toString();
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
}
