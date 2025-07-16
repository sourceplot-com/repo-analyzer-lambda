package com.sourceplot.init;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.sourceplot.model.RepoStats;
import com.sourceplot.init.annotations.AggregateStatsTableName;
import com.sourceplot.init.annotations.RepoStatsTableName;
import com.sourceplot.model.AggregateStats;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

public class AwsModule extends AbstractModule {
    @Override
    protected void configure() {
    }
    
    @Provides
    @Singleton
    public DynamoDbClient provideDynamoDbClient() {
        return DynamoDbClient.builder().build();
    }
    
    @Provides
    @Singleton
    public DynamoDbEnhancedClient provideDynamoDbEnhancedClient(DynamoDbClient dynamoDbClient) {
        return DynamoDbEnhancedClient.builder()
            .dynamoDbClient(dynamoDbClient)
            .build();
    }
    
    @Provides
    @Singleton
    public DynamoDbTable<RepoStats> provideRepoStatsTable(
        DynamoDbEnhancedClient enhancedClient,
        @RepoStatsTableName String tableName
    ) {
        return enhancedClient.table(tableName, TableSchema.fromBean(RepoStats.class));
    }
    
    @Provides
    @Singleton
    public DynamoDbTable<AggregateStats> provideAggregateStatsTable(
        DynamoDbEnhancedClient enhancedClient,
        @AggregateStatsTableName String tableName
    ) {
        return enhancedClient.table(tableName, TableSchema.fromBean(AggregateStats.class));
    }
}
