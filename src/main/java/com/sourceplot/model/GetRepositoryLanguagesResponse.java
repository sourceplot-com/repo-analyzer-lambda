package com.sourceplot.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.Map;

import org.immutables.value.Value;

@Value.Immutable
@Value.Style(init = "with*", get = { "get*", "is*" }, jdkOnly = true)
@JsonSerialize(as = ImmutableGetRepositoryLanguagesResponse.class)
@JsonDeserialize(as = ImmutableGetRepositoryLanguagesResponse.class)
public interface GetRepositoryLanguagesResponse {
    Map<String, Integer> languages();
}
