import java.io.BufferedReader
import java.io.InputStreamReader

plugins {
    id("com.gradleup.shadow") version("9.3.1")
    id("com.modrinth.minotaur") version ("2.+")
    id("xyz.jpenilla.run-paper") version ("3.0.2")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")

    implementation(project(":common:impl"))
    implementation(project(":platform:paper:api"))
    implementation("org.lushplugins.chatcolorhandler:paper:8.1.0")
    implementation("io.github.revxrsal:lamp.bukkit:4.0.0-rc.16")
}

tasks {
    processResources {
        expand(project.properties)

        inputs.property("version", rootProject.version)
        filesMatching("plugin.yml") {
            expand("version" to rootProject.version)
        }
    }

    shadowJar {
        relocate("org.lushplugins.lushlib", "org.lushplugins.pluginupdater.libraries.lushlib")

        archiveFileName.set("${project.name}-${project.version}.jar")
    }

    runServer {
        minecraftVersion("1.21")

        downloadPlugins {
            modrinth("viaversion", "5.7.1") // ViaVersion
            modrinth("viabackwards", "5.7.1") // ViaBackwards
            // The following plugins are intentionally outdated for testing purposes
            modrinth("djC8I9ui", "3.2.0") // LushRewards
            modrinth("discordsrv", "1.27.0") // DiscordSRV
            modrinth("coreprotect", "22.3") // CoreProtect
            modrinth("fancynpcs", "2.2.2") // FancyNPCs
            modrinth("fancyholograms", "2.3.1") // FancyHolograms
            modrinth("nMwMeNFr", "2025.07") // UltimateAutoRestart
        }
    }
}

modrinth {
    token.set(System.getenv("MODRINTH_TOKEN"))
    projectId.set("IBSpJfbm")
    if (System.getenv("RELEASE_TYPE") == "release") {
        versionNumber.set(rootProject.version.toString())
        changelog.set(getChangelogSinceLastTag())
    } else {
        versionNumber.set("${rootProject.version}-${getCurrentCommitHash()}")
    }
    uploadFile.set(file("build/libs/${project.name}-${project.version}.jar"))
    versionType.set(System.getenv("RELEASE_TYPE"))
    gameVersions.addAll(
        "1.21.4", "1.21.5", "1.21.6", "1.21.7", "1.21.8", "1.21.9", "1.21.10", "1.21.11",
        "21.6"
    )
    loaders.addAll("paper", "purpur", "folia")
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

fun getLastTag(): String {
    return ProcessBuilder("git", "describe", "--tags", "--abbrev=0")
        .start().inputStream.bufferedReader().readText().trim()
}

fun getChangelogSinceLastTag(): String {
    return ProcessBuilder("git", "log", "${getLastTag()}..HEAD", "--pretty=format:* %s ([#%h](https://github.com/OakLoaf/PluginUpdater/commit/%H))")
        .start().inputStream.bufferedReader().readText().trim()
}