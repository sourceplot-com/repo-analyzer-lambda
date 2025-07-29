package com.sourceplot.init;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.sourceplot.init.EnvironmentModule.EnvironmentConfig;
import com.sourceplot.stats.aggregate.AggregateStatsItem;
import com.sourceplot.stats.repo.RepoStatsItem;

import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

public class AwsModule extends AbstractModule {
    @Override
    protected void configure() {
    }
    
    private DynamoDbClient getDynamoDbClient() {
        return DynamoDbClient.builder().build();
    }
    
    @Provides
    @Singleton
    public DynamoDbEnhancedClient provideDynamoDbEnhancedClient() {
        return DynamoDbEnhancedClient.builder()
            .dynamoDbClient(getDynamoDbClient())
            .build();
    }
    
    @Provides
    @Singleton
    public DynamoDbTable<RepoStatsItem> provideRepoStatsTable(
        DynamoDbEnhancedClient enhancedClient,
        EnvironmentConfig environmentConfig
    ) {
        return enhancedClient.table(environmentConfig.repoStatsTableName(), TableSchema.fromBean(RepoStatsItem.class));
    }
    
    @Provides
    @Singleton
    public DynamoDbTable<AggregateStatsItem> provideAggregateStatsTable(
        DynamoDbEnhancedClient enhancedClient,
        EnvironmentConfig environmentConfig
    ) {
        return enhancedClient.table(environmentConfig.aggregateStatsTableName(), TableSchema.fromBean(AggregateStatsItem.class));
    }
}
