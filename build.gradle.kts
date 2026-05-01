import java.io.BufferedReader
import java.io.InputStreamReader

plugins {
    `java-library`
    `maven-publish`
    id("com.gradleup.shadow") version("9.3.1")
}

group = "org.lushplugins"
version = "2.2.0"

allprojects {
    apply(plugin = "java-library")
    apply(plugin = "maven-publish")

    group = rootProject.group
    version = rootProject.version

    repositories {
        mavenLocal()
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/") // Paper
        maven("https://repo.lushplugins.org/snapshots") // LushLib
    }

    java {
        toolchain.languageVersion.set(JavaLanguageVersion.of(21))

        withSourcesJar()
    }

    tasks {
        withType<JavaCompile> {
            options.encoding = "UTF-8"
        }
    }

    publishing {
        repositories {
            maven {
                name = "lushReleases"
                url = uri("https://repo.lushplugins.org/releases")
                credentials(PasswordCredentials::class)
                authentication {
                    isAllowInsecureProtocol = true
                    create<BasicAuthentication>("basic")
                }
            }

            maven {
                name = "lushSnapshots"
                url = uri("https://repo.lushplugins.org/snapshots")
                credentials(PasswordCredentials::class)
                authentication {
                    isAllowInsecureProtocol = true
                    create<BasicAuthentication>("basic")
                }
            }
        }
    }
}

fun getCurrentCommitHash(): String {
    val process = ProcessBuilder("git", "rev-parse", "--short", "HEAD").start()
    val reader = BufferedReader(InputStreamReader(process.inputStream))
    val commitHash = reader.readLine()
    reader.close()
    process.waitFor()
    if (process.exitValue() == 0) {
        return commitHash ?: ""
    } else {
        throw IllegalStateException("Failed to retrieve the commit hash.")
    }
}

fun getLastTag(): String {
    return ProcessBuilder("git", "describe", "--tags", "--abbrev=0")
        .start().inputStream.bufferedReader().readText().trim()
}

fun getChangelogSinceLastTag(): String {
    return ProcessBuilder("git", "log", "${getLastTag()}..HEAD", "--pretty=format:* %s ([#%h](https://github.com/OakLoaf/PluginUpdater/commit/%H))")
        .start().inputStream.bufferedReader().readText().trim()
}
