pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "jvm-library-template"
include(":library")
include(":kabinet")
project(":kabinet").projectDir = file("../kabinet/library")