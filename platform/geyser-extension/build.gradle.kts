plugins {
    id("com.gradleup.shadow")
    id("updater.publish-modrinth")
}

dependencies {
    compileOnly("org.geysermc.geyser:api:2.11.3-SNAPSHOT")

    implementation(project(":common:impl"))
    implementation(project(":platform:geyser-api"))
//    implementation("io.github.revxrsal:lamp.velocity:4.0.0-rc.18")
//    implementation("io.github.revxrsal:lamp.brigadier:4.0.0-rc.18")

    testImplementation(project(":tests:common-plugins-test"))
    testImplementation("org.junit.jupiter:junit-jupiter:6.1.3")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks {
    shadowJar {
        archiveFileName.set("${rootProject.name}-geyser-${project.version}.jar")

        relocate("com.electronwill.nightconfig", "org.lushplugins.pluginupdater.libraries.nightconfig")
    }

    test {
        useJUnitPlatform()
    }
}