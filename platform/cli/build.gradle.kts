plugins {
    id("com.gradleup.shadow")
}

dependencies {
    api(project(":common:impl"))
    implementation("io.github.revxrsal:lamp.cli:4.0.0-rc.18")
    api("net.kyori:adventure-text-minimessage:5.2.0")
    implementation("net.kyori:adventure-text-serializer-ansi:5.2.0")
    api("com.electronwill.night-config:json:3.9.0")
}

tasks {
    jar {
        manifest.attributes(
            "Main-Class" to "org.lushplugins.pluginupdater.cli.PluginUpdaterCLI"
        )
    }

    shadowJar {
        archiveFileName.set("${rootProject.name}-CLI-${project.version}.jar")
    }
}

tasks.processResources {
    from(project(":platform:paper-plugin").file("src/main/resources/common-plugins.yml")) {
        rename { "paper-common-plugins.yml" }
    }
    from(project(":platform:velocity-plugin").file("src/main/resources/common-plugins.yml")) {
        rename { "velocity-common-plugins.yml" }
    }
}