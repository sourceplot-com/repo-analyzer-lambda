package com.sourceplot.handler;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.sourceplot.accessor.RepoStatsAccessor;
import com.sourceplot.external.github.LanguageDataParser;
import com.sourceplot.init.AwsModule;
import com.sourceplot.init.EnvironmentModule;
import com.sourceplot.init.ServiceModule;
import com.sourceplot.model.ActiveRepositoriesPayload;
import com.sourceplot.model.RepoStats;

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

import static com.google.inject.Guice.createInjector;

@Slf4j
public class RepoAnalysisHandler implements RequestHandler<SQSEvent, SQSBatchResponse> {
    private static final Metrics metrics = MetricsFactory.getMetricsInstance();

    @Inject
    private ObjectMapper objectMapper;
    @Inject
    private HttpClient httpClient;
    @Inject
    private RepoStatsAccessor repoStatsAccessor;
    @Inject
    private LanguageDataParser languageDataParser;
    @Inject
    private ExecutorService executorService;

    public RepoAnalysisHandler() {
        var injector = createInjector(new ServiceModule(), new EnvironmentModule(), new AwsModule());
        injector.injectMembers(this);
    }

    @Override
    @Logging
    @Tracing
    @FlushMetrics(captureColdStart = true)
    public SQSBatchResponse handleRequest(SQSEvent input, Context context) {
        log.info("Received request with {} messages", input.getRecords().size());
        metrics.addMetric("InputMessages", input.getRecords().size(), MetricUnit.COUNT);

        var start = System.currentTimeMillis();

        var failures = new ArrayList<SQSBatchResponse.BatchItemFailure>();

        var messagesBeingProcessed = new CountDownLatch(input.getRecords().size());
        for (SQSEvent.SQSMessage message : input.getRecords()) {
            log.info("Processing message: {}", message.getMessageId());

            executorService.execute(() -> {
                try {
                    log.info("Started processing message {} on thread {}", message.getMessageId(), Thread.currentThread().getName());
                    processMessage(message);
                    log.info("Completed processing message {}", message.getMessageId());
                } catch (Exception e) {
                    log.error("Error processing message {}: {}", message.getMessageId(), e.getMessage(), e);
                    synchronized (failures) {
                        failures.add(new SQSBatchResponse.BatchItemFailure(message.getMessageId()));
                    }
                } finally {
                    messagesBeingProcessed.countDown();
                    log.debug("Count down latch for message {}, remaining: {}", message.getMessageId(), messagesBeingProcessed.getCount());
                }
            });
        }

        try {
            log.info("Waiting for all {} remaining messages to be processed", messagesBeingProcessed.getCount());
            messagesBeingProcessed.await();
            log.info("All messages processed");
        } catch (InterruptedException e) {
            log.error("Fatal error waiting for messages to be processed", e);
        }

        var end = System.currentTimeMillis();
        metrics.addMetric("TotalProcessingTime", end - start, MetricUnit.MILLISECONDS);

        return new SQSBatchResponse(failures);
    }

    private void processMessage(SQSEvent.SQSMessage message) throws InterruptedException, IOException, JsonProcessingException {
        var start = System.currentTimeMillis();

        log.debug("Raw message body: {}", message.getBody());
        var payload = objectMapper.readValue(message.getBody(), ActiveRepositoriesPayload.class);
        log.info("Processing payload at timestamp {} with {} repositories", payload.timestamp(), payload.repositories().size());

        if (payload.repositories().isEmpty()) {
            log.warn("No repositories in payload, skipping processing");
            return;
        }

        var allRepoStats = new ArrayList<RepoStats>(payload.repositories().size());

        var futures = new ArrayList<CompletableFuture<Void>>(payload.repositories().size());
        log.info("Starting to create HTTP requests for {} repositories", payload.repositories().size());
        
        var position = 1;
        for (var repository : payload.repositories()) {
            log.debug("Creating request {}/{} for repository: {}", position, payload.repositories().size(), repository.name());
            var languagesUri = URI.create(String.format("https://api.github.com/repos/%s/languages", repository.name()));
            var request = HttpRequest.newBuilder()
                    .uri(languagesUri)
                    .header("Accept", "application/json")
                    .header("User-Agent", "sourceplot-repo-analyzer/1.0")
                    .timeout(Duration.ofSeconds(10))
                    .build();

            CompletableFuture<Void> future = httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    log.debug("Received response for repository: {}, status: {}", repository.name(), response.statusCode());
                    try {
                        if (response.statusCode() == 403) {
                            log.warn("Rate limited by GitHub API for repository {}: {} - {}", repository.name(), response.statusCode(), response.body());
                            return;
                        } else if (response.statusCode() != 200) {
                            log.warn("Non-200 response for repository {}: {} - {}", repository.name(), response.statusCode(), response.body());
                            return;
                        }

                        var bytesByLanguage = languageDataParser.parse(response.body());
                        var repoStats = RepoStats.builder()
                            .repo(repository.name())
                            .dateHour(RepoStatsAccessor.DATE_HOUR_FORMATTER.format(Instant.parse(payload.timestamp()).atZone(ZoneOffset.UTC)))
                            .bytesByLanguage(bytesByLanguage)
                            .build();

                        synchronized (allRepoStats) {
                            allRepoStats.add(repoStats);
                        }
                        log.debug("Successfully processed language data for repository: {}", repository.name());
                    }
                    catch (JsonProcessingException e) {
                        log.error("Error parsing language data for repository: " + repository.name(), e);
                    }
                    catch (Exception e) {
                        log.error("Error handling language data response for repository: " + repository.name(), e);
                    }
                })
                .exceptionally(throwable -> {
                    log.error("HTTP request failed for repository: " + repository.name(), throwable);
                    return null;
                });

            futures.add(future);
            log.debug("Added future for repository: {}", repository.name());
            position++;
        }

        log.info("Created {} futures, waiting for all {} language data requests to complete", futures.size(), futures.size());

        try {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[futures.size()])).join();
            log.info("All HTTP requests completed successfully");
        } catch (Exception e) {
            log.error("Error waiting for HTTP requests to complete", e);
            throw e;
        }

        log.info("All repositories processed (collected {} repo stats), now saving repo stats objects in batch", allRepoStats.size());
        repoStatsAccessor.batchSaveRepoStats(allRepoStats);
        log.info("Successfully saved all repo stats items");

        var end = System.currentTimeMillis();
        metrics.addMetric("MessageProcessingTime", end - start, MetricUnit.MILLISECONDS);
        metrics.addMetric("SuccessfulRepoStats", allRepoStats.size(), MetricUnit.COUNT);
    }
}
