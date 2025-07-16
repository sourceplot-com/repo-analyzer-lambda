plugins {
    application
    id("com.gradleup.shadow") version "8.3.0"
}

repositories {
    mavenCentral()
}

dependencies {
    testCompileOnly(libs.lombok)
    testAnnotationProcessor(libs.lombok)
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    
    compileOnly(libs.lombok)
    compileOnly(libs.immutables.value)
    annotationProcessor(libs.lombok)
    annotationProcessor(libs.immutables.value)
    implementation(libs.aws.lambda.java.core)
    implementation(libs.aws.lambda.java.events)
    implementation(libs.aws.lambda.java.log4j2)
    implementation(platform(libs.aws.java.sdk.bom))
    implementation(libs.aws.java.sdk.dynamodb)
    implementation(libs.aws.java.sdk.dynamodb.enhanced)
    implementation(libs.guice)
    implementation(libs.jackson.core)
    implementation(libs.jackson.databind)
    implementation(libs.jackson.annotations)
    implementation(libs.jackson.datatype.jdk8)
    implementation(libs.log4j2.api)
    implementation(libs.log4j2.core)
    implementation(libs.log4j2.slf4j.impl)
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
