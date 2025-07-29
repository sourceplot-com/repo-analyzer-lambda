package com.sourceplot.stats.repo;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sourceplot.init.EnvironmentModule.EnvironmentConfig;

import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Singleton
public class RepoStatsAccessor {
    public static final DateTimeFormatter DATE_HOUR_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH");
    
    private final DynamoDbEnhancedClient enhancedClient;
    private final DynamoDbTable<RepoStatsItem> repoStatsTable;
    private final String dateIndexName;
    
    @Inject
    public RepoStatsAccessor(
        DynamoDbEnhancedClient enhancedClient,
        DynamoDbTable<RepoStatsItem> repoStatsTable,
        EnvironmentConfig environmentConfig
    ) {
        this.repoStatsTable = repoStatsTable;
        this.enhancedClient = enhancedClient;
        this.dateIndexName = environmentConfig.repoStatsTableDateIndex();
    }
    
    public void saveRepoStats(RepoStatsItem repoStats) {
        repoStatsTable.putItem(repoStats);
        log.info("Successfully saved repo stats for {}", repoStats.getRepo());
    }
    
    public Optional<RepoStatsItem> getRepoStats(String repoName, String dateHour) {
        log.info("Retrieving repo stats for repo: {} on datehour: {}", repoName, dateHour);
        
        return Optional.ofNullable(
            repoStatsTable.getItem(Key.builder()
                .partitionValue(repoName)
                .sortValue(dateHour)
                .build()
            )
        );
    }
    
    public Optional<RepoStatsItem> getRepoStats(String repoName, LocalDate date) {
        return getRepoStats(repoName, date.format(DATE_HOUR_FORMATTER));
    }
    
    public void batchSaveRepoStats(List<RepoStatsItem> repoStatsList) {
        if (repoStatsList.isEmpty()) {
            log.debug("No repo stats to batch save");
            return;
        }

        final int batchSize = 25;
        List<WriteBatch> batchWrites = new ArrayList<>(repoStatsList.size() / batchSize);

        for (int i = 0; i < repoStatsList.size(); i += batchSize) {
            var chunk = repoStatsList.subList(i, Math.min(i + batchSize, repoStatsList.size()));
            batchWrites.add(writeBatchWithLimit(chunk));
        }

        log.info("Batch saving {} repo stats items containing <= {} items", repoStatsList.size(), batchSize);
        enhancedClient.batchWriteItem(
            BatchWriteItemEnhancedRequest.builder()
                .writeBatches(batchWrites)
                .build()
        );
    }

    private WriteBatch writeBatchWithLimit(List<RepoStatsItem> repoStatsList) {
        var builder = WriteBatch.builder(RepoStatsItem.class)
            .mappedTableResource(repoStatsTable);

        for (var repoStats : repoStatsList) {
            builder.addPutItem(repoStats);
        }

        return builder.build();
    }
}
