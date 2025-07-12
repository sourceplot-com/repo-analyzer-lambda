package com.sourceplot.handler;

import java.util.ArrayList;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSBatchResponse;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class RepositoryQueueHandler implements RequestHandler<SQSEvent, SQSBatchResponse> {
    @Override
    public SQSBatchResponse handleRequest(SQSEvent input, Context context) {
        var failures = new ArrayList<SQSBatchResponse.BatchItemFailure>();

        for (SQSEvent.SQSMessage record : input.getRecords()) {
            log.info("Processing record: {}", record.getMessageId());
            failures.add(new SQSBatchResponse.BatchItemFailure(record.getMessageId()));
        }

        return new SQSBatchResponse(failures);
    }
}

