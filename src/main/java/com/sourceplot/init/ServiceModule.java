package com.sourceplot.init;

import java.net.http.HttpClient;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;

public class ServiceModule extends AbstractModule {
    @Override
    protected void configure() {
    }

    @Provides
    public ObjectMapper provideObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new Jdk8Module());
        return objectMapper;
    }

    @Provides
    public HttpClient provideHttpClient() {
        return HttpClient.newHttpClient();
    }

    @Provides
    public ExecutorService provideExecutorService() {
        return Executors.newFixedThreadPool(50);
    }
}
