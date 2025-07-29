package com.sourceplot.languagedata.github;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sourceplot.languagedata.LanguageData;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Singleton
public class GithubLanguageDataAccessor {
    @Inject
    private HttpClient httpClient;
    @Inject
    private GithubLanguageDataParser languageDataParser;

    public LanguageData getLanguageDataForRepository(String repositoryName) throws IOException, InterruptedException {
        var request = HttpRequest.newBuilder()
            .uri(getLanguageDataEndpoint(repositoryName))
            .header("Accept", "application/json")
            .header("User-Agent", "sourceplot-repo-analyzer-lambda")
            .timeout(Duration.ofSeconds(10))
            .build();

        var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        log.debug("Got raw response from Github: {}", response.body());

        return languageDataParser.parse(response.body());
    }

    private URI getLanguageDataEndpoint(String repositoryName) {
        return URI.create(String.format("https://api.github.com/repos/%s/languages", repositoryName));
    }
}
