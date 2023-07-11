plugins {
    id("de.fayard.refreshVersions") version "0.51.0"
}

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    pluginManagement {
        repositories {
            gradlePluginPortal()
            kotlinDevelopmentRepositories()
            composeDevelopmentRepositories()
        }
    }
    repositories {
        google()
        mavenCentral()
        kotlinDevelopmentRepositories()
        composeDevelopmentRepositories()
    }
}

fun RepositoryHandler.kotlinDevelopmentRepositories() {
    maven("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/bootstrap")
    maven("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/dev")
    maven("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/temporary")
    maven("https://maven.pkg.jetbrains.space/public/p/kotlinx-coroutines/maven")
    maven("https://maven.pkg.jetbrains.space/kotlin/p/wasm/experimental")
}

fun RepositoryHandler.composeDevelopmentRepositories() {
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    maven("https://androidx.dev/storage/compose-compiler/repository/")
}

refreshVersions {
    featureFlags {
        enable(de.fayard.refreshVersions.core.FeatureFlag.LIBS)
    }
}

rootProject.name = "compose-counting-grid"
