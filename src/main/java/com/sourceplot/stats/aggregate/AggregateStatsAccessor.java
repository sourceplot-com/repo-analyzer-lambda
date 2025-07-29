package com.sourceplot.stats.aggregate;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Slf4j
@Singleton
public class AggregateStatsAccessor {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    private final DynamoDbEnhancedClient enhancedClient;
    private final DynamoDbTable<AggregateStatsItem> aggregateStatsTable;
    
    @Inject
    public AggregateStatsAccessor(
        DynamoDbEnhancedClient enhancedClient,
        DynamoDbTable<AggregateStatsItem> aggregateStatsTable
    ) {
        this.enhancedClient = enhancedClient;
        this.aggregateStatsTable = aggregateStatsTable;
    }
    
    public void saveAggregateStats(AggregateStatsItem aggregateStats) {
        log.info("Saving aggregate stats for date: {}", aggregateStats.getDate());
        
        aggregateStatsTable.putItem(aggregateStats);
        log.info("Successfully saved aggregate stats for date: {}", aggregateStats.getDate());
    }
    
    public Optional<AggregateStatsItem> getAggregateStats(String date) {
        log.info("Retrieving aggregate stats for date: {}", date);
        
        return Optional.ofNullable(
            aggregateStatsTable.getItem(Key.builder()
                .partitionValue(date)
                .build()
        ));
    }
    
    public Optional<AggregateStatsItem> getAggregateStats(LocalDate date) {
        return getAggregateStats(date.format(DATE_FORMATTER));
    }
}
