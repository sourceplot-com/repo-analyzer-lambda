package com.sourceplot.init;

import org.immutables.value.Value;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;

public class EnvironmentModule extends AbstractModule {
    @Override
    protected void configure() {
    }

    @Value.Immutable
    @Value.Style(init = "with*", get = { "get*", "is*" })
    public interface EnvironmentConfig {
        String repoStatsTableName();
        String repoStatsTableDateIndex();
        String aggregateStatsTableName();
        int activeRepositoriesPerMessage();
        int repositoriesToProcessPerLambda();
        int messagesToProcessPerLambda();
        int messagesToProcessConcurrently();
    }

    @Provides
    @Singleton
    public EnvironmentConfig provideEnvironmentConfig() {
        return ImmutableEnvironmentConfig.builder()
            .withRepoStatsTableName(System.getenv("REPO_STATS_TABLE"))
            .withRepoStatsTableDateIndex(System.getenv("REPO_STATS_TABLE_DATE_INDEX"))
            .withAggregateStatsTableName(System.getenv("AGGREGATE_STATS_TABLE"))
            .withActiveRepositoriesPerMessage(Integer.parseInt(System.getenv("ACTIVE_REPOSITORIES_PER_MESSAGE")))
            .withRepositoriesToProcessPerLambda(Integer.parseInt(System.getenv("REPOSITORIES_TO_PROCESS_PER_LAMBDA")))
            .withMessagesToProcessPerLambda(Integer.parseInt(System.getenv("MESSAGES_TO_PROCESS_PER_LAMBDA")))
            .withMessagesToProcessConcurrently(Integer.parseInt(System.getenv("MESSAGES_TO_PROCESS_CONCURRENTLY")))
            .build();
    }
}
