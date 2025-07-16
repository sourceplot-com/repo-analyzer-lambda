package com.sourceplot.init;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.sourceplot.init.annotations.AggregateStatsTableName;
import com.sourceplot.init.annotations.RepoStatsTableDateIndex;
import com.sourceplot.init.annotations.RepoStatsTableName;

public class EnvironmentModule extends AbstractModule {
    @Override
    protected void configure() {
    }

    @Provides
    @RepoStatsTableName
    public String provideRepoStatsTableName() {
        return System.getenv("REPO_STATS_TABLE");
    }

    @Provides
    @RepoStatsTableDateIndex
    public String provideDateIndex() {
        return System.getenv("REPO_STATS_TABLE_DATE_INDEX");
    }

    @Provides
    @AggregateStatsTableName
    public String provideAggregateStatsTableName() {
        return System.getenv("AGGREGATE_STATS_TABLE");
    }
}
