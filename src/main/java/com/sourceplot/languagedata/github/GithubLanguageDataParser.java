package com.sourceplot.languagedata.github;

import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sourceplot.languagedata.ImmutableLanguageData;
import com.sourceplot.languagedata.LanguageData;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
class GithubLanguageDataParser {
    private final ObjectMapper objectMapper;

    @Inject
    public GithubLanguageDataParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public LanguageData parse(String languageDataJson) throws JsonProcessingException {
        var bytesByLanguage = objectMapper.readValue(languageDataJson, new TypeReference<Map<String, Integer>>() {});

        return ImmutableLanguageData.of(bytesByLanguage);
    }
}
