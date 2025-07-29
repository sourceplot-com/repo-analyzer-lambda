package com.sourceplot.languagedata;

import java.util.Map;

import org.immutables.value.Value;

@Value.Immutable
@Value.Style(init = "with*", get = { "get*", "is*" })
public interface LanguageData {
    @Value.Parameter
    Map<String, Integer> bytesByLanguage();
}
