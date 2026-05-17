plugins {
    id("com.gradleup.shadow")
    id("xyz.jpenilla.run-paper")
    id("updater.publish-modrinth")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")

    implementation(project(":common:impl"))
    implementation(project(":platform:paper-api"))
    implementation("org.lushplugins.chatcolorhandler:paper:8.1.0")
    implementation("io.github.revxrsal:lamp.bukkit:4.0.0-rc.16")
}

tasks {
    processResources {
        inputs.property("version", rootProject.version)
        filesMatching("plugin.yml") {
            expand("version" to rootProject.version)
        }
    }

    shadowJar {
        archiveFileName.set("${rootProject.name}-${project.version}.jar")

        relocate("com.electronwill.nightconfig", "org.lushplugins.pluginupdater.libraries.nightconfig")
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
    loaders.addAll("paper", "purpur", "folia")
}