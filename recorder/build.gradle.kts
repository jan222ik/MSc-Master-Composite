import org.jetbrains.compose.compose

plugins {
    kotlin("jvm")
    id("org.jetbrains.compose")
}

group = "com.github.jan222ik"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("org.apache.poi:poi-ooxml:5.2.2")
    implementation(compose.desktop.currentOs)
    implementation(compose.materialIconsExtended)
    implementation(compose.ui)
    implementation(compose.uiTooling)
    implementation(compose.foundation)
    implementation("org.jetbrains.compose.ui:ui-util:1.1.0-alpha05")
}