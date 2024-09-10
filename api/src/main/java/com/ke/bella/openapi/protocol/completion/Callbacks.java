package com.ke.bella.openapi.protocol.completion;

import com.ke.bella.openapi.EndpointProcessData;
import com.ke.bella.openapi.protocol.ChannelException;
import com.ke.bella.openapi.protocol.OpenapiResponse;
import com.ke.bella.openapi.protocol.log.EndpointLogger;
import com.ke.bella.openapi.utils.DateTimeUtils;
import com.ke.bella.openapi.utils.SseHelper;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.ArrayList;
import java.util.List;

public interface Callbacks {

     @FunctionalInterface
     interface SseEventConverter<T> {
         T convert(String id, String event, String msg);
     }
     class StreamCompletionCallback {
         public StreamCompletionCallback(SseEmitter sse, EndpointProcessData processData, EndpointLogger logger) {
             this.sse = sse;
             this.processData = processData;
             this.logger = logger;
             this.responses = new ArrayList<>();
         }

         private final SseEmitter sse;
         private final EndpointProcessData processData;
         private final EndpointLogger logger;
         private final List<StreamCompletionResponse> responses;

         public void callback(StreamCompletionResponse msg) {
             SseHelper.sendEvent(sse, msg);
             responses.add(msg);
         }

         public void done() {
             SseHelper.sendEvent(sse, "[DONE]");
         }

         public void finish() {
             sse.complete();
             log();
         }

         public void finish(ChannelException exception) {
             OpenapiResponse.OpenapiError openapiError = OpenapiResponse.convertFromException(exception);
             StreamCompletionResponse response = StreamCompletionResponse.builder()
                     .created(DateTimeUtils.getCurrentMills())
                     .error(openapiError)
                     .build();
             try {
                 callback(response);
             } finally {
                 sse.completeWithError(exception);
                 log();
             }
         }


         private void log() {
             CompletionResponse response;
             long firstPackageTime;
             long endTime;
             if(CollectionUtils.isEmpty(responses)) {
                 response = new CompletionResponse();
                 firstPackageTime = DateTimeUtils.getCurrentMills();
                 endTime = firstPackageTime;
             } else {
                 response = CompletionResponse.aggregate(responses);
                 firstPackageTime = responses.get(0).getCreated();
                 endTime = responses.get(responses.size() - 1).getCreated();
             }
             response.setCreated(endTime);
             processData.setFirstPackageTime(firstPackageTime);
             processData.setResponse(response);
             logger.log(processData);
         }
     }
}
