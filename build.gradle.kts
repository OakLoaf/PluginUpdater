import java.io.BufferedReader
import java.io.InputStreamReader

plugins {
    `java-library`
    `maven-publish`
    id("com.github.johnrengelman.shadow") version ("8.1.1")
    id("xyz.jpenilla.run-paper") version("2.3.1")
    id("com.modrinth.minotaur") version ("2.+")
}

allprojects {
    apply(plugin = "maven-publish")

    group = "org.lushplugins"
    version = "1.0.12"

    repositories {
        mavenCentral()
        mavenLocal()
        maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/") // Spigot
        maven("https://repo.lushplugins.org/releases") // LushLib
        maven("https://repo.lushplugins.org/snapshots") // LushLib
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

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.21.5-R0.1-SNAPSHOT")

    implementation(project(":api"))

    implementation("org.lushplugins:LushLib:0.10.71")
    implementation("io.github.revxrsal:lamp.common:4.0.0-rc.12")
    implementation("io.github.revxrsal:lamp.bukkit:4.0.0-rc.12")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))

    withSourcesJar()
}

tasks {
    withType<JavaCompile> {
        options.encoding = "UTF-8"
    }

    shadowJar {
        relocate("org.lushplugins.lushlib", "org.lushplugins.pluginupdater.libraries.lushlib")

        minimize()

        val folder = System.getenv("pluginFolder")
        if (folder != null) destinationDirectory.set(file(folder))
        archiveFileName.set("${project.name}-${project.version}.jar")
    }

    processResources {
        expand(project.properties)

        inputs.property("version", rootProject.version)
        filesMatching("plugin.yml") {
            expand("version" to rootProject.version)
        }
    }

    runServer {
        minecraftVersion("1.21")

        downloadPlugins {
            modrinth("viaversion", "5.0.3")
            modrinth("viabackwards", "5.0.3")
            // The following plugins are intentionally outdated
            modrinth("djC8I9ui", "3.2.0") // LushRewards
            modrinth("discordsrv", "1.27.0")
            modrinth("coreprotect", "22.3")
            modrinth("fancynpcs", "2.2.2")
            modrinth("fancyholograms", "2.3.1")
        }
    }
}

tasks.withType(xyz.jpenilla.runtask.task.AbstractRun::class) {
    javaLauncher = javaToolchains.launcherFor {
        vendor = JvmVendorSpec.JETBRAINS
        languageVersion = JavaLanguageVersion.of(21)
    }

    jvmArgs("-XX:+AllowEnhancedClassRedefinition")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = rootProject.group.toString() + ".pluginupdater"
            artifactId = rootProject.name
            version = rootProject.version.toString()
            from(project.components["java"])
        }
    }
}

modrinth {
    token.set(System.getenv("MODRINTH_TOKEN"))
    projectId.set("IBSpJfbm")
    if (System.getenv("RELEASE_TYPE") == "release") {
        versionNumber.set(rootProject.version.toString())
    } else {
        versionNumber.set(rootProject.version.toString() + "-" + getCurrentCommitHash())
        print(rootProject.version.toString() + "-" + getCurrentCommitHash())
    }
    uploadFile.set(file("build/libs/${project.name}-${project.version}.jar"))
    versionType.set(System.getenv("RELEASE_TYPE"))
    gameVersions.addAll(
        "1.18", "1.18.1", "1.18.2",
        "1.19", "1.19.1", "1.19.2", "1.19.3", "1.19.4",
        "1.20", "1.20.1", "1.20.2", "1.20.3", "1.20.4", "1.20.5", "1.20.6",
        "1.21", "1.21.1", "1.21.2", "1.21.3", "1.21.4", "1.21.5"
    )
    loaders.addAll("spigot", "paper", "purpur")
    syncBodyFrom.set(rootProject.file("README.md").readText())
}

tasks.modrinth {
    dependsOn("shadowJar")
    dependsOn(tasks.modrinthSyncBody)
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
