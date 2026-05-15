plugins {
    id("com.gradleup.shadow")
    id("xyz.jpenilla.run-velocity")
    id("updater.publish-modrinth")
}

dependencies {
    compileOnly("com.velocitypowered:velocity-api:3.5.0-SNAPSHOT")
    annotationProcessor("com.velocitypowered:velocity-api:3.5.0-SNAPSHOT")

    implementation(project(":common:impl"))
    implementation(project(":platform:velocity-api"))
    implementation("io.github.revxrsal:lamp.velocity:4.0.0-rc.16")
    implementation("io.github.revxrsal:lamp.brigadier:4.0.0-rc.16")
}

tasks {
    shadowJar {
        archiveFileName.set("${rootProject.name}-${project.version}.jar")

        relocate("com.electronwill.nightconfig", "org.lushplugins.pluginupdater.libraries.nightconfig")
    }

    runVelocity {
        velocityVersion("3.5.0-SNAPSHOT")

        downloadPlugins {
            modrinth("viaversion", "5.7.1") // ViaVersion
            modrinth("viabackwards", "5.7.1") // ViaBackwards
            // The following plugins are intentionally outdated for testing purposes
        }
    }
}

modrinth {
    loaders.addAll("velocity")
}