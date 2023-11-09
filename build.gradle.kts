import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

plugins {
    kotlin("multiplatform")
    if (System.getProperty("application.useJs") == "true") {
        id("org.jetbrains.compose") version "1.5.10"
    } else {
        id("org.jetbrains.compose") version "1.5.10-dev-wasm02"
    }
}

buildscript {
    dependencies {
        classpath("org.jetbrains.kotlinx:atomicfu-gradle-plugin:_")
    }
}

apply(plugin = "kotlinx-atomicfu")

group = "com.example"
version = "0.0-SNAPSHOT"

val useJs = System.getProperty("application.useJs") == "true"
val optimizeAggressively = System.getProperty("application.optimize") == "true"

kotlin {
    jvmToolchain(11)

    jvm {
        withJava()
    }

    if (useJs) {
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
    } else {
        wasmJs {
            moduleName = "app"
            binaries.executable()
            browser {
                commonWebpackConfig {
                    outputFileName = "$moduleName.js"
                    devServer = (devServer ?: KotlinWebpackConfig.DevServer()).copy(
                        port = 8081,
                        static = (devServer?.static ?: mutableListOf()).apply {
                            // Serve sources to debug inside browser
                            add(project.rootDir.path)
                        }
                    )
                }
            }

            applyBinaryen {
                binaryenArgs = mutableListOf(
                    "--enable-nontrapping-float-to-int",
                    "--enable-gc",
                    "--enable-reference-types",
                    "--enable-exception-handling",
                    "--enable-bulk-memory",
                    "--inline-functions-with-loops",
                    "--traps-never-happen",
                    "--fast-math"
                )

                binaryenArgs += if (optimizeAggressively) {
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
    }

    sourceSets {
        all {
            languageSettings {
                languageVersion = "2.0"
                progressiveMode = true
                optIn("kotlin.RequiresOptIn")
            }
        }

        val commonMain by getting {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material)
                implementation("org.jetbrains.kotlinx:atomicfu:_")
                implementation(KotlinX.datetime)
            }
        }

        val jvmMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
            }
        }

        if (useJs) {
            val jsMain by getting {
                dependencies {
                    implementation(compose.web.core)
                }
            }
        }
    }
}

compose {
    desktop.application.mainClass = "MainKt"
    experimental {
        web.application {}
    }
}

if (!useJs) {
    rootProject.tasks {
        val hackNodeModuleImports by registering(Copy::class) {
            group = "kotlin browser"
            mustRunAfter("kotlinNpmInstall")
            from(buildDir.path + "/js/node_modules/@js-joda")
            into(buildDir.path + "/js/packages/app/kotlin/@js-joda")
        }
        for (dependent in listOf("wasmJsBrowserProductionRun", "wasmJsBrowserDevelopmentRun")) {
            named(dependent) {
                dependsOn(hackNodeModuleImports)
            }
        }
    }
}

val yarnExecutablePath: String by lazy {
    with(rootProject.extensions.getByType<org.jetbrains.kotlin.gradle.targets.js.yarn.YarnRootExtension>()) {
        requireConfigured().executable.substringBeforeLast(".js")
    }
}

val nodeBinaryDirectory: String by lazy {
    with(rootProject.extensions.getByType<org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootExtension>()) {
        requireConfigured().nodeBinDir.toString()
    }
}

fun Exec.addNodePath() {
    environment("PATH", "${System.getenv("PATH")}:$nodeBinaryDirectory")
}

rootProject.tasks {
    val yarnLockName = "yarn.lock"
    val yarnBuildDirectory = "$rootDir/build/js"
    val yarnLockBuildPath = "$yarnBuildDirectory/$yarnLockName"
    val yarnLockPrimaryName = "$yarnLockName.primary"
    val yarnLockStorageDirectory = "$rootDir/kotlin-js-store"
    val yarnLockPrimaryPath = "$yarnLockStorageDirectory/$yarnLockPrimaryName"

    val yarnShowAuditReport by registering(Exec::class) {
        group = "nodejs"
        description = "Shows an audit report for npm packages, listing known vulnerabilities."
        workingDir = File(yarnBuildDirectory)
        addNodePath()
        commandLine = mutableListOf(yarnExecutablePath, "audit")
    }

    val yarnShowOutdatedPackages by registering(Exec::class) {
        group = "nodejs"
        description = "Shows outdated npm packages."
        workingDir = File(yarnBuildDirectory)
        addNodePath()
        commandLine = mutableListOf(yarnExecutablePath, "outdated")
        isIgnoreExitValue = true
    }

    val yarnLockUpdatePrimary by registering {
        group = "nodejs"
        description =
            "Updates '$yarnLockPrimaryName' from the build-generated '$yarnLockName'. Must be invoked manually."

        doLast {
            copy {
                from(yarnLockBuildPath)
                rename { yarnLockPrimaryName }
                into(yarnLockStorageDirectory)
            }
        }

        inputs.file(yarnLockBuildPath).withPropertyName("inputFile")
        outputs.file(yarnLockPrimaryPath).withPropertyName("outputFile")
    }

    val yarnLockRestore by registering {
        group = "nodejs"
        description = "Restores '$yarnLockName' from '$yarnLockPrimaryName' to ensure stable builds."

        // Kotlin >=1.6.10 restores 'yarn.lock' from 'kotlin-js-store/yarn.lock', but also updates the latter
        // unconditionally, without any checks performed. To avoid green-lighting unchecked code, make sure
        // our version always gets precedence.
        mustRunAfter("kotlinRestoreYarnLock")

        doLast {
            copy {
                from(yarnLockPrimaryPath)
                rename { yarnLockName }
                into(yarnBuildDirectory)
            }
        }

        inputs.file(yarnLockPrimaryPath).withPropertyName("inputFile")
        outputs.file(yarnLockBuildPath).withPropertyName("outputFile")
    }

    val yarnLockValidate by registering {
        group = "nodejs"
        description = (
            "Validates that the build directory's '$yarnLockName' corresponds" +
                " to '$yarnLockPrimaryName' in the project root directory."
            )
        dependsOn("kotlinNpmInstall")

        doLast {
            val expected = File(yarnLockPrimaryPath).readText().trim()
            val actual = File(yarnLockBuildPath).readText().trim()

            if (expected != actual) {
                // WORKAROUND https://youtrack.jetbrains.com/issue/IDEA-267343 –
                //     'idea diff ...' produces an exception and does not immediately complete, although it does
                //     open the diff window.
                //     Replace the following workaround with the code in comments when the issue is fixed.
                ProcessBuilder("idea", "diff", yarnLockPrimaryPath, yarnLockBuildPath)
                    .redirectOutput(ProcessBuilder.Redirect.INHERIT)
                    .redirectError(ProcessBuilder.Redirect.DISCARD)
                    .start()
                /* Replace with:
                exec {
                    commandLine = mutableListOf("idea", "diff", yarnLockPrimaryPath, yarnLockBuildPath)
                }
                */

                throw AssertionError(
                    "The build-generated '$yarnLockName' differs from '$yarnLockPrimaryName'" +
                        " in the project root directory." +
                        " Each difference indicates a dependency update which has not been confirmed by" +
                        " running './gradlew :yarnLockUpdatePrimary'.\n" +
                        "\tAn idea diff window has been opened.\n" +
                        "\tTo explore differences later, please run:" +
                        " idea diff '$yarnLockPrimaryPath' '$yarnLockBuildPath'\n" +
                        "\tTo assess package risks, please run: gradlew :analyseSupplyChain"
                )
            }
        }
    }

    named<org.jetbrains.kotlin.gradle.targets.js.npm.tasks.KotlinNpmInstallTask>("kotlinNpmInstall") {
        dependsOn(yarnLockRestore)
        finalizedBy(yarnLockValidate)

        // Avoid package installation scripts vulnerability:
        // https://blog.npmjs.org/post/141702881055/package-install-scripts-vulnerability
        args += "--ignore-scripts"
        // To detect packages, which use installation scripts, see 'can-i-ignore-scripts', described here:
        // https://dev.to/naugtur/get-safe-and-remain-productive-with-can-i-ignore-scripts-2ddc
    }

    val analyzeNpmSupplyChain by registering(Exec::class) {
        group = "nodejs"
        description = "Analyses the npm package supply chain, hinting on possible security risks."

        dependsOn("kotlinNpmInstall")

        val packagesToExclude = listOf("packages", "packages_imported").flatMap { packageDirectory ->
            File("$yarnBuildDirectory/$packageDirectory")
                .listFiles()?.mapNotNull { if (it.isDirectory) it.name else null } ?: listOf()
        }

        commandLine = mutableListOf(
            "sca",
            "--exclude",
            packagesToExclude.joinToString("|"),
            "$yarnBuildDirectory/node_modules"
        )
    }

    named("check") {
        dependsOn(":yarnLockValidate")
    }
}
