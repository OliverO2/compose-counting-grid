plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
}

group = "com.${rootProject.name}"
version = "0.0-SNAPSHOT"

// If a Compose compiler release compatible with the intended Kotlin compiler version is missing,
// select a pre-release compiler from https://androidx.dev/storage/compose-compiler/repository.
// Otherwise, use an empty string.
val composeCompilerVersion: String = "" // "1.2.1-dev-k1.7.10-27cf0868d10"

val composeCompilerArgs: List<String> = listOf(
    "-P",
    "plugin:androidx.compose.compiler.plugins.kotlin:suppressKotlinVersionCompatibilityCheck=true"
)

if (composeCompilerVersion.isNotEmpty()) {
    configurations.all {
        resolutionStrategy.dependencySubstitution {
            substitute(module("org.jetbrains.compose.compiler:compiler"))
                .using(module("androidx.compose.compiler:compiler:$composeCompilerVersion"))
                .because("using the compose prerelease compiler")
        }
    }
}

if (composeCompilerArgs.isNotEmpty()) {
    tasks.withType<org.jetbrains.kotlin.gradle.dsl.KotlinCompile<*>>().configureEach {
        kotlinOptions.freeCompilerArgs += composeCompilerArgs
    }
}

val frontendJvmArgs: MutableList<String> = mutableListOf(
    // "-Xlog:gc*=info:file=$rootDir/build/tmp/frontend-gc.log:tags,time,uptime,level"
)

val javaLanguageVersion = JavaLanguageVersion.of(11)

java {
    toolchain.languageVersion.set(javaLanguageVersion)
}

compose {
    desktop.application.mainClass = "MainKt"
    experimental.web.application {}
}

kotlin {
    jvm("frontendJvm") {
        compilations.all {
            kotlinOptions {
                jvmTarget = javaLanguageVersion.toString()
            }
        }
        withJava()
    }

    js("frontendJs", IR) {
        browser {
            useCommonJs()
            binaries.executable()
        }
    }

    sourceSets {
        all {
            languageSettings.apply {
                progressiveMode = true
                optIn("kotlin.RequiresOptIn")
            }
        }

        @Suppress("UNUSED_VARIABLE")
        val commonMain by getting {
            dependencies {
                implementation(compose.material)
                implementation(KotlinX.datetime)
            }
        }

        @Suppress("UNUSED_VARIABLE")
        val frontendJvmMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
            }
        }

        @Suppress("UNUSED_VARIABLE")
        val frontendJsMain by getting {
            dependencies {
                implementation(compose.web.core)
            }
        }
    }
}

tasks {
    // WORKAROUND "Execution optimizations have been disabled for task ':frontendJsProcessResources'"
    named("frontendJsProcessResources") {
        dependsOn("unpackSkikoWasmRuntimeFrontendJs")
    }
    named("frontendJsBrowserProductionRun") {
        dependsOn("frontendJsProductionExecutableCompileSync")
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

    @Suppress("UNUSED_VARIABLE")
    val yarnShowAuditReport by registering(Exec::class) {
        group = "nodejs"
        description = "Shows an audit report for npm packages, listing known vulnerabilities."
        workingDir = File(yarnBuildDirectory)
        addNodePath()
        commandLine = mutableListOf(yarnExecutablePath, "audit")
    }

    @Suppress("UNUSED_VARIABLE")
    val yarnShowOutdatedPackages by registering(Exec::class) {
        group = "nodejs"
        description = "Shows outdated npm packages."
        workingDir = File(yarnBuildDirectory)
        addNodePath()
        commandLine = mutableListOf(yarnExecutablePath, "outdated")
        isIgnoreExitValue = true
    }

    @Suppress("UNUSED_VARIABLE")
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

    @Suppress("UNUSED_VARIABLE")
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
