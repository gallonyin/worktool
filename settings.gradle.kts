pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
    }
}

rootProject.name = "worktool"

// Core modules
include(":app")
include(":core")
include(":common")

// Ensure all modules use Kotlin DSL
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
