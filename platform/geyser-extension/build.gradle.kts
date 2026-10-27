plugins {
    id("com.gradleup.shadow")
    id("updater.publish-modrinth")
}

dependencies {
    compileOnly("org.geysermc.geyser:api:2.11.3-SNAPSHOT")

    implementation(project(":common:impl"))
    implementation(project(":platform:geyser-api"))
    implementation("io.github.revxrsal:lamp.geyser:4.0.0")

    testImplementation(project(":tests:common-plugins-test"))
    testImplementation("org.junit.jupiter:junit-jupiter:6.1.3")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks {
    processResources {
        inputs.property("version", rootProject.version)
        inputs.property("commit", rootProject.getCurrentCommitHash())

        filesMatching("extension.yml") {
            expand(
                "version" to rootProject.version,
                "commit" to rootProject.getCurrentCommitHash()
            )
        }
    }

    shadowJar {
        archiveFileName.set("${rootProject.name}-geyser-${project.version}.jar")

        relocate("com.electronwill.nightconfig", "org.lushplugins.pluginupdater.libraries.nightconfig")
    }

    test {
        useJUnitPlatform()
    }
}