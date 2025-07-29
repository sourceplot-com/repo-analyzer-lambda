package com.sourceplot.handler;

import java.util.ArrayList;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.sourceplot.ingest.ActiveRepositoriesPayload;
import com.sourceplot.ingest.ActiveRepositoriesProcessor;
import com.sourceplot.ingest.RepositoryProcessingException;
import com.sourceplot.init.AwsModule;
import com.sourceplot.init.ConcurrencyModule;
import com.sourceplot.init.EnvironmentModule;
import com.sourceplot.init.ServiceModule;

import lombok.extern.slf4j.Slf4j;
import software.amazon.lambda.powertools.logging.Logging;
import software.amazon.lambda.powertools.tracing.Tracing;
import software.amazon.lambda.powertools.metrics.FlushMetrics;
import software.amazon.lambda.powertools.metrics.Metrics;
import software.amazon.lambda.powertools.metrics.MetricsFactory;
import software.amazon.lambda.powertools.metrics.model.MetricUnit;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSBatchResponse;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.amazonaws.services.lambda.runtime.events.SQSEvent.SQSMessage;

import static com.google.inject.Guice.createInjector;

@Slf4j
public class RepoAnalysisHandler implements RequestHandler<SQSEvent, SQSBatchResponse> {
    private static final Metrics metrics = MetricsFactory.getMetricsInstance();

    @Inject
    private ObjectMapper objectMapper;
    @Inject
    private ActiveRepositoriesProcessor processor;

    public RepoAnalysisHandler() {
        var injector = createInjector(new EnvironmentModule(), new AwsModule(), new ConcurrencyModule(), new ServiceModule());
        injector.injectMembers(this);
    }

    @Override
    @Logging
    @Tracing
    @FlushMetrics(captureColdStart = true)
    public SQSBatchResponse handleRequest(SQSEvent input, Context context) {
        log.info("Received request with {} messages", input.getRecords().size());
        metrics.addMetric("InputMessages", input.getRecords().size(), MetricUnit.COUNT);

        var failures = new ArrayList<SQSBatchResponse.BatchItemFailure>();

        for (var i = 0; i < input.getRecords().size(); i++) {
            var message = input.getRecords().get(i);
            log.info("Processing message ({}/{}): {}", i + 1, input.getRecords().size(), message.getMessageId());

            try {
                processMessage(message);
            } catch (Exception e) {
                log.error("Error processing message {}", message.getMessageId(), e);
                failures.add(new SQSBatchResponse.BatchItemFailure(message.getMessageId()));
            }
        }

        log.info("All messages processed");

        return new SQSBatchResponse(failures);
    }

    private void processMessage(SQSMessage message) throws JsonProcessingException, RepositoryProcessingException {
        log.debug("Raw message body: {}", message.getBody());

        var payload = objectMapper.readValue(message.getBody(), ActiveRepositoriesPayload.class);
        processor.process(payload);
    }
}
