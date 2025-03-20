// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.8.0")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.22")
        classpath("com.google.dagger:hilt-android-gradle-plugin:2.48")
        classpath("org.jetbrains.kotlin:kotlin-serialization:1.9.22")
    }
}

subprojects {
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        kotlinOptions {
            jvmTarget = "17"
            freeCompilerArgs = listOf(
                "-Xjsr305=strict",
                "-Xno-param-assertions",
                "-Xno-call-assertions"
            )
        }
    }
}

tasks.register("clean") {
    doLast {
        project.delete(layout.buildDirectory)
    }
}
