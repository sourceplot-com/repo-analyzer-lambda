package com.sourceplot.ingest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sourceplot.languagedata.LanguageDataFetchingException;
import com.sourceplot.languagedata.LanguageDataRepository;
import com.sourceplot.stats.repo.RepoStatsAccessor;
import com.sourceplot.stats.repo.RepoStatsItem;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Singleton
public class ActiveRepositoriesProcessor {
    @Inject
    private ExecutorService executorService;
    @Inject
    private RepoStatsAccessor repoStatsAccessor;
    @Inject
    private LanguageDataRepository languageDataRepository;

    public void process(ActiveRepositoriesPayload payload) throws RepositoryProcessingException {
        if (payload.repositories().isEmpty()) {
            log.warn("No repositories in payload, skipping processing");
            return;
        }

        log.info("Processing payload at timestamp {} with {} repositories", payload.timestamp(), payload.repositories().size());

        var repoStatsItems = Collections.synchronizedList(new ArrayList<RepoStatsItem>());

        var repositoriesToProcess = new CountDownLatch(payload.repositories().size());
        for (var repository : payload.repositories()) {
            executorService.execute(() -> {
                try {
                    var languageData = languageDataRepository.getLanguageDataForRepository(repository.name());
                    if (languageData.bytesByLanguage().isEmpty()) {
                        log.warn("Language data for repository {} is empty, skipping", repository.name());
                        return;
                    }

                    repoStatsItems.add(
                        RepoStatsItem.builder()
                            .repo(repository.name())
                            .dateHour(payload.timestamp())
                            .bytesByLanguage(languageData.bytesByLanguage())
                            .build()
                    );
                } catch (LanguageDataFetchingException e) {
                    throw new RuntimeException(String.format(
                        "Failed to fetch language data for repository %s", repository.name()
                    ), e);
                } finally {
                    repositoriesToProcess.countDown();
                }
            });
        }

        log.info("Waiting for all repositories {} to be processed", repositoriesToProcess.getCount());
        try {
            repositoriesToProcess.await();
        } catch (Exception e) {
            throw new RepositoryProcessingException("Failed to wait for all repositories to be processed", e);
        }

        log.info("All repositories processed, saving to DDB");
        repoStatsAccessor.batchSaveRepoStats(repoStatsItems);
        log.info("All repositories have been saved to DDB");
    }
}
