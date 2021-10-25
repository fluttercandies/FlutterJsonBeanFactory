import org.jetbrains.changelog.markdownToHTML
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

fun properties(key: String): String = project.findProperty(key).toString()

plugins {
    // Java support
    id("java")
    // Kotlin support
    id("org.jetbrains.kotlin.jvm") version "1.5.31"
    // Gradle IntelliJ Plugin
    id("org.jetbrains.intellij") version "0.7.3"
    // Gradle Changelog Plugin
    id("org.jetbrains.changelog") version "1.3.0"
}

group = properties("pluginGroup")
version = properties("pluginVersion")

buildscript {
    repositories {
        mavenLocal()
        mavenCentral()
    }
    dependencies {
        classpath("org.jetbrains.intellij.plugins:gradle-intellij-plugin:0.7.3")
    }
}
apply(plugin = "org.jetbrains.intellij")

repositories {
    mavenLocal()
    mavenCentral()
}


intellij {
    pluginName = properties("pluginName")
    version = properties("platformVersion")
    type = properties("platformType")
    downloadSources = properties("platformDownloadSources").toBoolean()
    updateSinceUntilBuild = true
    setPlugins("yaml", "java", "Dart:203.7759", "io.flutter:61.2.2", "Kotlin")
//    setPlugins(properties("platformPlugins").split(',').map(String::trim).filter(String::isNotEmpty))
}
// Configure Gradle Changelog Plugin - read more: https://github.com/JetBrains/gradle-changelog-plugin
changelog {
    version.set(properties("pluginVersion"))
    groups.set(emptyList())
}

tasks {
    // Set the JVM compatibility versions
    properties("javaVersion").let {
        withType<JavaCompile> {
            sourceCompatibility = it
            targetCompatibility = it
        }
        withType<KotlinCompile> {
            kotlinOptions.jvmTarget = it
        }
    }

    wrapper {
        gradleVersion = properties("gradleVersion")
    }

    patchPluginXml {
        setVersion(properties("pluginVersion"))
        setSinceBuild(properties("pluginSinceBuild"))
        setUntilBuild(properties("pluginUntilBuild"))

        // Extract the <!-- Plugin description --> section from README.md and provide for the plugin's manifest
        setPluginDescription(
            projectDir.resolve("README.md").readText().lines().run {
                val start = "<!-- Plugin description -->"
                val end = "<!-- Plugin description end -->"

                if (!containsAll(listOf(start, end))) {
                    throw GradleException("Plugin description section not found in README.md:\n$start ... $end")
                }
                subList(indexOf(start) + 1, indexOf(end))
            }.joinToString("\n").run { markdownToHTML(this) }
        )

        // Get the latest available change notes from the changelog file
        changeNotes(provider {
            changelog.run {
                getOrNull(properties("pluginVersion")) ?: getLatest()
            }.toHTML()
        })
    }

    runPluginVerifier {
        ideVersions(properties("pluginVerifierIdeVersions").split(',').map(String::trim).filter(String::isNotEmpty))
    }

    // Configure UI tests plugin
    // Read more: https://github.com/JetBrains/intellij-ui-test-robot
    runIdeForUiTests {
        systemProperty("robot-server.port", "8082")
        systemProperty("ide.mac.message.dialogs.as.sheets", "false")
        systemProperty("jb.privacy.policy.text", "<!--999.999-->")
        systemProperty("jb.consents.confirmation.enabled", "false")
    }

//    signPlugin {
//        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
//        privateKey.set(System.getenv("PRIVATE_KEY"))
//        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
//    }

    publishPlugin {
        dependsOn("patchChangelog")
        token(System.getenv("ORG_GRADLE_PROJECT_intellijPublishToken"))
        // pluginVersion is based on the SemVer (https://semver.org) and supports pre-release labels, like 2.1.7-alpha.3
        // Specify pre-release label to publish the plugin in a custom Release Channel automatically. Read more:
        // https://plugins.jetbrains.com/docs/intellij/deployment.html#specifying-a-release-channel
        setChannels(listOf(properties("pluginVersion").split('-').getOrElse(1) { "default" }.split('.').first()))
    }
}


dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.5.31")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.5.31")
    implementation("org.jetbrains:annotations:19.0.0")
    implementation("org.yaml:snakeyaml:1.29")
}

fun itemPlugin(startName: String): String =
    intellij.plugins.toList().first { it.toString().contains(startName) }.toString()
println(
    "\nBuilding plugin ${properties("pluginVersion")} for IDEA " +
            "version ${properties("platformVersion")}\n"
)
println("Since: ${properties("pluginSinceBuild")}")
println("Until: ${properties("pluginUntilBuild")}")
println("Dart: ${itemPlugin("Dart")}")
println("Flutter: ${itemPlugin("flutter")}")
println("Artifacts output directory: ${rootProject.buildDir}\n")