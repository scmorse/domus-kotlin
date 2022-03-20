import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.6.10"
    kotlin("plugin.serialization") version "1.6.10"
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.0")
    implementation("io.ktor:ktor-client-websockets:1.6.7")
    implementation("io.ktor:ktor-client-cio:1.6.7")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.2")
}

group = "me.stephenmorse"
version = "1.0.0"

repositories {
    mavenCentral()
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "1.8"
}