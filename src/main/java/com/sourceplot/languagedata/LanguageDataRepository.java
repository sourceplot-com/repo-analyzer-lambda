package com.sourceplot.languagedata;

import java.io.IOException;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sourceplot.languagedata.github.GithubLanguageDataAccessor;

@Singleton
public class LanguageDataRepository {
    @Inject
    private GithubLanguageDataAccessor githubLanguageDataAccessor;

    public LanguageData getLanguageDataForRepository(String repositoryName) throws LanguageDataFetchingException {
        try {
            return githubLanguageDataAccessor.getLanguageDataForRepository(repositoryName);
        } catch (IOException | InterruptedException e) {
            throw new LanguageDataFetchingException(String.format("Failed to fetch language data for repository %s", repositoryName), e);
        }
    }
}
