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

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

application {
    mainClass.set("com.sourceplot.handler.RepoAnalysisHandler")
}

dependencies {
    data class PackageGroup(val name: String, val version: String = "") {
        fun with(artifact: String) = with(artifact, version)
        fun with(artifact: String, version: String) = "$name:$artifact:$version"
        fun withJust(artifact: String) = with(artifact, "")
    }

    val awsLambda = PackageGroup("com.amazonaws")
    val awsLambdaCoreVersion = "1.2.3"
    val awsLambdaEventsVersion = "3.15.0"
    val awsLambdaLog4j2Version = "1.6.0"

    val awsSdk = PackageGroup("software.amazon.awssdk", "2.31.77")
    val powertools = PackageGroup("software.amazon.lambda", "2.2.0")
    val log4j = PackageGroup("org.apache.logging.log4j", "2.24.3")
    val jacksonCore = PackageGroup("com.fasterxml.jackson.core", "2.18.3")
    val jacksonDatatype = PackageGroup("com.fasterxml.jackson.datatype", "2.18.3")
    val guice = PackageGroup("com.google.inject", "7.0.0")
    val aspectj = PackageGroup("org.aspectj", "1.9.24")
    val immutables = PackageGroup("org.immutables", "2.10.1")
    val lombok = PackageGroup("org.projectlombok", "1.18.30")
    val junit = PackageGroup("org.junit.jupiter", "5.13.3")
    val junitPlatform = PackageGroup("org.junit.platform")
    val slf4j = PackageGroup("org.slf4j", "2.0.17")

    compileOnly(immutables.with("value"))
    compileOnly(lombok.with("lombok"))
    annotationProcessor(immutables.with("value"))
    annotationProcessor(lombok.with("lombok"))
    aspect(powertools.with("powertools-logging-log4j"))
    aspect(powertools.with("powertools-tracing"))
    aspect(powertools.with("powertools-metrics"))

    implementation(aspectj.with("aspectjrt"))
    implementation(awsLambda.with("aws-lambda-java-core", awsLambdaCoreVersion))
    implementation(awsLambda.with("aws-lambda-java-events", awsLambdaEventsVersion))
    implementation(platform(awsSdk.with("bom")))
    implementation(awsSdk.withJust("dynamodb"))
    implementation(awsSdk.withJust("dynamodb-enhanced"))
    implementation(jacksonCore.with("jackson-core"))
    implementation(jacksonCore.with("jackson-databind"))
    implementation(jacksonCore.with("jackson-annotations"))
    implementation(jacksonDatatype.with("jackson-datatype-jdk8"))
    implementation(guice.with("guice"))
    implementation(log4j.with("log4j-core"))
    implementation(log4j.with("log4j-layout-template-json"))
    implementation(log4j.with("log4j-slf4j2-impl"))
    implementation(slf4j.with("slf4j-api"))

    testCompileOnly(immutables.with("value"))
    testCompileOnly(lombok.with("lombok"))
    testAnnotationProcessor(immutables.with("value"))
    testAnnotationProcessor(lombok.with("lombok"))
    testImplementation(junit.with("junit-jupiter"))
    testRuntimeOnly(junitPlatform.withJust("junit-platform-launcher"))
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}

tasks.shadowJar {
    transform(Log4j2PluginsCacheFileTransformer::class.java)
}
