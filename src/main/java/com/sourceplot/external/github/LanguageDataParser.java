package com.sourceplot.external.github;

import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
public class LanguageDataParser {
    private final ObjectMapper objectMapper;

    @Inject
    public LanguageDataParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public Map<String, Integer> parse(String json) throws JsonProcessingException {
        return objectMapper.readValue(json, new TypeReference<Map<String, Integer>>() {});
    }
}
