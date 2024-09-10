package com.ke.bella.openapi.protocol.log;

import com.ke.bella.openapi.EndpointProcessData;
import com.ke.bella.openapi.db.log.LogRepo;
import com.lmax.disruptor.EventHandler;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class LogRecordHandler implements EventHandler<LogEvent> {
    private final List<LogRepo> logRepos;

    public LogRecordHandler(List<LogRepo> logRepos) {
        this.logRepos = logRepos;
    }

    @Override
    public void onEvent(LogEvent event, long sequence, boolean endOfBatch) throws Exception {;
        logRepos.forEach(logRepo -> logRepo.record(event.getData()));
    }

    @Data
    @Builder
    public static class RecordLogInfo {
        private EndpointProcessData log;
        private String repositoryCode;
    }
}
