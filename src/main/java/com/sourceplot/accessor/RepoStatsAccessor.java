package com.sourceplot.accessor;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sourceplot.init.annotations.RepoStatsTableDateIndex;
import com.sourceplot.model.RepoStats;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Singleton
public class RepoStatsAccessor {
    public static final DateTimeFormatter DATE_HOUR_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH");
    
    private final DynamoDbEnhancedClient enhancedClient;
    private final DynamoDbTable<RepoStats> repoStatsTable;
    private final String dateIndexName;
    
    @Inject
    public RepoStatsAccessor(
        DynamoDbEnhancedClient enhancedClient,
        DynamoDbTable<RepoStats> repoStatsTable,
        @RepoStatsTableDateIndex String dateIndexName
    ) {
        this.repoStatsTable = repoStatsTable;
        this.enhancedClient = enhancedClient;
        this.dateIndexName = dateIndexName;
    }
    
    public void saveRepoStats(RepoStats repoStats) {
        repoStatsTable.putItem(repoStats);
        log.info("Successfully saved repo stats for {}", repoStats.getRepo());
    }
    
    public Optional<RepoStats> getRepoStats(String repoName, String dateHour) {
        log.info("Retrieving repo stats for repo: {} on datehour: {}", repoName, dateHour);
        
        return Optional.ofNullable(
            repoStatsTable.getItem(Key.builder()
                .partitionValue(repoName)
                .sortValue(dateHour)
                .build()
            )
        );
    }
    
    public Optional<RepoStats> getRepoStats(String repoName, LocalDate date) {
        return getRepoStats(repoName, date.format(DATE_HOUR_FORMATTER));
    }
    
    public void batchSaveRepoStats(List<RepoStats> repoStatsList) {
        if (repoStatsList.isEmpty()) {
            log.debug("No repo stats to batch save");
            return;
        }
        
        log.info("Batch saving {} repo stats items", repoStatsList.size());
        
        List<WriteBatch> writeBatches = repoStatsList.stream()
            .map(repoStats -> WriteBatch.builder(RepoStats.class)
                .mappedTableResource(repoStatsTable)
                .addPutItem(repoStats)
                .build()
            )
            .collect(Collectors.toList());
        
        enhancedClient.batchWriteItem(
            BatchWriteItemEnhancedRequest.builder()
                .writeBatches(writeBatches)
                .build()
        );

        log.info("Successfully batch saved {} repo stats items", repoStatsList.size());
    }
}
