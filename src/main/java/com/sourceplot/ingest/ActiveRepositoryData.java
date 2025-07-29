package com.sourceplot.ingest;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import org.immutables.value.Value;

@Value.Immutable
@Value.Style(init = "with*", get = { "get*", "is*" }, jdkOnly = true)
@JsonSerialize(as = ImmutableActiveRepositoryData.class)
@JsonDeserialize(as = ImmutableActiveRepositoryData.class)
public interface ActiveRepositoryData {
    String name();
}
