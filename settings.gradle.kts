pluginManagement {
    repositories {
        gradlePluginPortal()
        maven { url = uri("https://maven.pkg.jetbrains.space/public/p/compose/dev") }
    }

}
rootProject.name = "MSc-Master-Composite"
include("data")
include("combined")
include("forked-libs")
