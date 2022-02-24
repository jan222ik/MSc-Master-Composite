import org.jetbrains.compose.compose
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    val kotlinVersion = "1.6.10"
    kotlin("jvm") version kotlinVersion
    kotlin("kapt") version kotlinVersion
    id("org.jetbrains.compose") version "1.1.0-alpha05"
    id("org.jetbrains.dokka") version "1.6.10"
}

subprojects {
    plugins.apply("org.jetbrains.dokka")
    repositories {
        jcenter()
        mavenCentral()
        google()
        maven { url = uri("https://jitpack.io") }
        maven { url = uri("https://maven.pkg.jetbrains.space/public/p/compose/dev") }
    }
}

group = "com.github.jan222ik"
version = "1.0.0"

repositories {
    jcenter()
    mavenCentral()
    google()
    maven { url = uri("https://jitpack.io") }
    maven { url = uri("https://maven.pkg.jetbrains.space/public/p/compose/dev") }
}

val daggerVersion by extra("2.39.1")

dependencies {

    // Dagger : A fast dependency injector for Android and Java.
    kapt("com.google.dagger:dagger-compiler:$daggerVersion")
    kaptTest("com.google.dagger:dagger-compiler:$daggerVersion")

    // Dokka: Kotlin Documentation Plugin
    dokkaHtmlPlugin("org.jetbrains.dokka:kotlin-as-java-plugin:1.6.10")
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "11"
}


tasks.dokkaHtmlMultiModule.configure {
    outputDirectory.set(buildDir.resolve("dokka-doc"))
}