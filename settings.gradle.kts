dependencyResolutionManagement {
    repositories {
        maven("https://repo.papermc.io/repository/maven-public/") // Paper, Velocity
        maven("https://repo.lushplugins.org/snapshots") // ChatColorHandler
        mavenCentral()
    }
}

pluginManagement {
    plugins {
        id("com.gradleup.shadow") version "9.4.1"
        id("net.kyori.blossom") version "2.2.0"
        id("org.jetbrains.gradle.plugin.idea-ext") version "1.4.1"
        id("com.modrinth.minotaur") version "2.+"
        id("xyz.jpenilla.run-paper") version "3.0.2"
    }
}

rootProject.name = "PluginUpdater"

includeBuild("build-logic")

include("common:api")
include("common:impl")
include("platform:paper-api")
include("platform:paper-plugin")
include("platform:velocity-api")
include("platform:velocity-plugin")