package com.sourceplot.model;

import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

@Value.Immutable
@Value.Style(init = "with*", get = { "get*", "is*" }, jdkOnly = true)
@JsonSerialize(as = ImmutableActiveRepositoriesPayload.class)
@JsonDeserialize(as = ImmutableActiveRepositoriesPayload.class)
public interface ActiveRepositoriesPayload {
    String timestamp();
    List<ActiveRepositoryData> repositories();
}
