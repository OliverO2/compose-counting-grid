import org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

plugins {
    alias(libs.plugins.org.jetbrains.kotlin.multiplatform)
    alias(libs.plugins.org.jetbrains.compose)
}

buildscript {
    dependencies {
        classpath(libs.org.jetbrains.kotlinx.atomicfu.gradle.plugin)
    }
}

apply(plugin = "kotlinx-atomicfu")

group = "com.example"
version = "0.0-SNAPSHOT"

val useJs = System.getProperty("application.useJs") == "true"
val optimizeAggressively = System.getProperty("application.optimize") == "true"

kotlin {
    val jdkVersion = project.property("local.jdk.version").toString().toInt()

    jvmToolchain(jdkVersion)

    jvm {
        compilations.configureEach {
            kotlinOptions.freeCompilerArgs += listOf("-Xjdk-release=$jdkVersion")
        }
    }

    jvm {
        withJava()
    }

    js {
        moduleName = "app"
        binaries.executable()
        browser {
            useCommonJs()
            commonWebpackConfig {
                outputFileName = "$moduleName.js"
            }
        }
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        moduleName = "app"
        binaries.executable()
        browser {
            commonWebpackConfig {
                outputFileName = "$moduleName.js"
                devServer =
                    (devServer ?: KotlinWebpackConfig.DevServer()).copy(
                        port = 8081,
                        static =
                            (devServer?.static ?: mutableListOf()).apply {
                                // Serve sources to debug inside browser
                                add(project.rootDir.path)
                            }
                    )
            }
        }

        applyBinaryen {
            binaryenArgs =
                mutableListOf(
                    "--enable-nontrapping-float-to-int",
                    "--enable-gc",
                    "--enable-reference-types",
                    "--enable-exception-handling",
                    "--enable-bulk-memory",
                    "--inline-functions-with-loops",
                    "--traps-never-happen",
                    "--fast-math"
                )

            binaryenArgs +=
                if (optimizeAggressively) {
                    listOf(
                        "--closed-world",
                        // "--metrics",
                        "-O3",
                        "--gufa", // "--metrics",
                        "-O3",
                        "--gufa", // "--metrics",
                        "-O3",
                        "--gufa" // "--metrics"
                    )
                } else {
                    listOf(
                        "-O1",
                        "-c" // Run passes while binary size decreases
                    )
                }
        }
    }

    sourceSets {
        all {
            languageSettings {
                progressiveMode = true
                optIn("kotlin.RequiresOptIn")
            }
        }

        val commonMain by getting {
            dependencies {
                implementation(compose.ui)
                implementation(compose.foundation)
                implementation(compose.material)
                implementation(libs.org.jetbrains.kotlinx.atomicfu)
                implementation(libs.org.jetbrains.kotlinx.kotlinx.datetime)
            }
        }

        val jvmMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
            }
        }

        val jsMain by getting {
            dependencies {
                @Suppress("DEPRECATION")
                implementation(compose.web.core) // Required for Compose Web/Canvas on JS
            }
        }
    }
}

compose {
    providers.gradleProperty("local.compose.kotlinCompilerPlugin").orNull?.let { composeKotlinCompilerPlugin ->
        kotlinCompilerPlugin.set(composeKotlinCompilerPlugin)
        val kotlinVersion = "${libs.plugins.org.jetbrains.kotlin.multiplatform.get().version}"
        kotlinCompilerPluginArgs.add("suppressKotlinVersionCompatibilityCheck=$kotlinVersion")
    }
    // kotlinCompilerPluginArgs.add("reportsDestination=${layout.buildDirectory.file("reports")}")

    desktop.application.mainClass = "MainKt"

    experimental {
        web.application {}
    }
}

// Hack to use kotlinx-datetime with Wasm
rootProject.tasks {
    val hackNodeModuleImports by registering(Copy::class) {
        group = "kotlin browser"
        mustRunAfter("kotlinNpmInstall")
        from(layout.buildDirectory.file("js/node_modules/@js-joda"))
        into(layout.buildDirectory.file("js/packages/app/kotlin/@js-joda"))
    }
    for (dependent in listOf("wasmJsBrowserProductionRun", "wasmJsBrowserDevelopmentRun")) {
        named(dependent) {
            dependsOn(hackNodeModuleImports)
        }
    }
}
