/*
 * This file was generated by the Gradle 'init' task.
 */

plugins {
    `java-library`
    `maven-publish`
    kotlin("jvm")
    kotlin("kapt") version "1.8.0"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.maven.apache.org/maven2/")
}

dependencies {
    api(libs.org.jetbrains.kotlin.kotlin.stdlib.jdk8)
    testImplementation(libs.org.jetbrains.kotlin.kotlin.test)
    compileOnly(libs.com.velocitypowered.velocity.api)
    kapt(libs.com.velocitypowered.velocity.api)
    implementation(kotlin("stdlib-jdk8"))
}

group = "me.yellowbear"
version = "1.0-SNAPSHOT"
description = "uwuAuth"

publishing {
    publications.create<MavenPublication>("maven") {
        from(components["java"])
    }
}

tasks.withType<JavaCompile>() {
    options.encoding = "UTF-8"
}

tasks.withType<Javadoc>() {
    options.encoding = "UTF-8"
}
