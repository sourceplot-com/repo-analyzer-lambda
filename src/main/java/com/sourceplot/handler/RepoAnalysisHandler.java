package com.sourceplot.handler;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.sourceplot.accessor.RepoStatsAccessor;
import com.sourceplot.init.AwsModule;
import com.sourceplot.init.EnvironmentModule;
import com.sourceplot.init.ServiceModule;
import com.sourceplot.model.ActiveRepositoriesPayload;
import com.sourceplot.model.RepoStats;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSBatchResponse;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;

import static com.google.inject.Guice.createInjector;

public class RepoAnalysisHandler implements RequestHandler<SQSEvent, SQSBatchResponse> {
    private static final Logger LOG = LogManager.getLogger(RepoAnalysisHandler.class);

    @Inject
    private ObjectMapper objectMapper;
    @Inject
    private HttpClient httpClient;
    @Inject
    private RepoStatsAccessor repoStatsAccessor;

    public RepoAnalysisHandler() {
        Injector injector = createInjector(new ServiceModule(), new EnvironmentModule(), new AwsModule());
        injector.injectMembers(this);
    }

    @Override
    public SQSBatchResponse handleRequest(SQSEvent input, Context context) {
        context.getLogger().log("Test lambda context logger");
        LOG.info("Test log4j2 logger");

        var failures = new ArrayList<SQSBatchResponse.BatchItemFailure>();

        for (SQSEvent.SQSMessage message : input.getRecords()) {
            try {
                processMessage(message);
            } catch (Exception e) {
                LOG.error("Error processing message: {}", e.getMessage());
                failures.add(new SQSBatchResponse.BatchItemFailure(message.getMessageId()));
            }
        }

        return new SQSBatchResponse(failures);
    }

    private void processMessage(SQSEvent.SQSMessage message) throws InterruptedException, IOException, JsonProcessingException {
        var payload = objectMapper.readValue(message.getBody(), ActiveRepositoriesPayload.class);
        LOG.info("Processing payload: {}", payload);

        for (var repository : payload.repositories()) {
            var languagesUri = URI.create(String.format("https://api.github.com/repos/%s/languages", repository.name()));
            var languagesResponse = httpClient.send(HttpRequest.newBuilder().uri(languagesUri).build(), HttpResponse.BodyHandlers.ofString());
            LOG.info("Languages response: {}", languagesResponse.body());

            repoStatsAccessor.saveRepoStats(
                RepoStats.builder()
                    .repo(repository.name())
                    .date(payload.timestamp())
                    .languageData(languagesResponse.body())
                    .build()
            );
        }
    }
}

