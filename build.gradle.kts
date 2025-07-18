import com.github.jengelman.gradle.plugins.shadow.transformers.Log4j2PluginsCacheFileTransformer

plugins {
    java
    application
    id("com.gradleup.shadow") version "8.3.0"
    id("io.freefair.aspectj.post-compile-weaving") version "8.14"
}

repositories {
    mavenCentral()
}

dependencies {
    aspect(libs.powertools.logging)
    aspect(libs.powertools.tracing)
    aspect(libs.powertools.metrics)

    // AWS Lambda Core
    implementation(libs.aws.lambda.java.core)
    implementation(libs.aws.lambda.java.events)
    implementation(libs.aws.lambda.java.log4j2)
    
    // AWS SDK BOM and services
    implementation(platform(libs.aws.java.sdk.bom))
    implementation(libs.aws.java.sdk.dynamodb)
    implementation(libs.aws.java.sdk.dynamodb.enhanced)
    
    // Logging
    implementation(libs.slf4j.api)
    implementation(libs.log4j2.core)
    implementation(libs.log4j2.layout.template.json)
    implementation(libs.log4j2.slf4j2.impl)
    
    // Jackson JSON processing
    implementation(libs.jackson.core)
    implementation(libs.jackson.databind)
    implementation(libs.jackson.annotations)
    implementation(libs.jackson.datatype.jdk8)
    
    // Dependency Injection
    implementation(libs.guice)
    
    // AspectJ
    implementation(libs.aspectj.aspectjrt)
    
    // Code generation
    compileOnly(libs.immutables.value)
    annotationProcessor(libs.immutables.value)
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
    
    // Testing
    testImplementation(libs.junit.jupiter)
    testCompileOnly(libs.lombok)
    testAnnotationProcessor(libs.lombok)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

application {
    mainClass.set("com.sourceplot.handler.RepoAnalysisHandler")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}

tasks.shadowJar {
    transform(Log4j2PluginsCacheFileTransformer::class.java)
}
