plugins {
    kotlin("jvm") version "1.9.0"
    kotlin("plugin.noarg") version "1.9.0"
    kotlin("plugin.spring") version "1.9.0"
    kotlin("plugin.allopen") version "1.9.0"
    kotlin("plugin.jpa") version "1.9.0"
    id("org.springframework.boot") version "3.3.5"
    id("io.spring.dependency-management") version "1.1.6"
}

group = "ru.slivoviy"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

repositories {
    mavenCentral()
    maven("https://packages.confluent.io/maven/")
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:2.0.0")
    implementation("org.springframework.boot:spring-boot-starter-web:3.3.2")
    implementation("org.springframework.boot:spring-boot-starter-jdbc:3.3.2")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa:3.3.2")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.17.1")
    implementation("org.hibernate.orm:hibernate-core:6.2.25.Final")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation ("io.github.microutils:kotlin-logging-jvm:3.0.5")
    implementation("ch.qos.logback:logback-classic:1.5.6")

    implementation("org.springframework.kafka:spring-kafka:3.2.4")

    implementation("jakarta.persistence:jakarta.persistence-api:3.1.0")
    implementation("org.postgresql:postgresql")

    implementation("software.amazon.awssdk:s3:2.29.9")
    implementation("software.amazon.awssdk:auth:2.29.9")

    testImplementation("org.springframework.boot:spring-boot-starter-test:3.3.2")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("org.springframework.kafka:spring-kafka-test:3.2.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.10.3")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
}

allOpen {
    annotation("ru.slivoviy.api.internal.annotation.NoArg")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
    jvmToolchain(17)
}

tasks.withType<Test> {
    useJUnitPlatform()
}
