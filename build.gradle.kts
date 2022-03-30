import org.jetbrains.compose.compose
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    val kotlinVersion = "1.6.10"
    kotlin("jvm") version kotlinVersion
    kotlin("kapt") version kotlinVersion
    id("org.jetbrains.compose") version "1.1.1"
    id("org.jetbrains.dokka") version kotlinVersion
    id("de.comahe.i18n4k") version "0.3.0"
}

subprojects {
    plugins.apply("org.jetbrains.dokka")
    repositories {
        jcenter()
        mavenCentral()
        google()
        maven { url = uri("https://jitpack.io") }
        maven { url = uri("https://maven.pkg.jetbrains.space/public/p/compose/dev") }
        maven { url = uri("https://oss.sonatype.org/content/repositories/snapshots/") }
        maven { url = uri("https://dist.wso2.org/maven2/") }
        maven { url = uri("https://repo1.maven.org/maven2/") }
    }
}

group = "com.github.jan222ik"
version = "1.0.0"

repositories {
    jcenter()
    mavenCentral()
    mavenLocal()
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

    // i18n
    //implementation("de.comahe.i18n4k:i18n4k-core:0.3.0")
    implementation("de.comahe.i18n4k:i18n4k-core-jvm:0.3.0")
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "11"
    kotlinOptions.freeCompilerArgs = listOf("-opt-in=kotlin.RequiresOptIn")
}


tasks.dokkaHtmlMultiModule.configure {
    outputDirectory.set(buildDir.resolve("dokka-doc"))
}