plugins {
    application
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
    implementation(libs.aws.java.sdk.dynamodb)
    implementation(libs.guava)
    implementation(libs.log4j2.api)
    implementation(libs.log4j2.core)
    implementation(libs.log4j2.slf4j.impl)
    implementation(libs.aws.lambda.java.log4j2)
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

tasks.register<Jar>("fatJar") {
    archiveFileName.set("github-data-extractor.jar")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    
    manifest {
        attributes["Main-Class"] = "com.sourceplot.handler.RepositoryQueueHandler"
    }
    
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    with(tasks.jar.get() as CopySpec)
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}
