package com.sourceplot.accessor;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sourceplot.model.AggregateStats;
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
    private final DynamoDbTable<AggregateStats> aggregateStatsTable;
    
    @Inject
    public AggregateStatsAccessor(
        DynamoDbEnhancedClient enhancedClient,
        DynamoDbTable<AggregateStats> aggregateStatsTable
    ) {
        this.enhancedClient = enhancedClient;
        this.aggregateStatsTable = aggregateStatsTable;
    }
    
    public void saveAggregateStats(AggregateStats aggregateStats) {
        log.info("Saving aggregate stats for date: {}", aggregateStats.getDate());
        
        aggregateStatsTable.putItem(aggregateStats);
        log.info("Successfully saved aggregate stats for date: {}", aggregateStats.getDate());
    }
    
    public Optional<AggregateStats> getAggregateStats(String date) {
        log.info("Retrieving aggregate stats for date: {}", date);
        
        return Optional.ofNullable(
            aggregateStatsTable.getItem(Key.builder()
                .partitionValue(date)
                .build()
        ));
    }
    
    public Optional<AggregateStats> getAggregateStats(LocalDate date) {
        return getAggregateStats(date.format(DATE_FORMATTER));
    }
}
