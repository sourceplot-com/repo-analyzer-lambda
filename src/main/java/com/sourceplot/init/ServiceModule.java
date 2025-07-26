package com.sourceplot.init;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.sourceplot.init.EnvironmentModule.EnvironmentConfig;

public class ServiceModule extends AbstractModule {
    @Override
    protected void configure() {
    }

    @Provides
    @Singleton
    public ObjectMapper provideObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new Jdk8Module());
        return objectMapper;
    }

    @Provides
    @Singleton
    public ExecutorService provideExecutorService(EnvironmentConfig environmentConfig) {
        return Executors.newFixedThreadPool(environmentConfig.messagesToProcessConcurrently());
    }

    @Provides
    @Singleton
    public HttpClient provideHttpClient(EnvironmentConfig environmentConfig) {
        return HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
            .executor(Executors.newVirtualThreadPerTaskExecutor())
            .version(HttpClient.Version.HTTP_2)
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();
    }
}
