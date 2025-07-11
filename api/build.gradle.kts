plugins {
    `java-library`
    id("com.gradleup.shadow") version("8.3.0")
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.21.7-R0.1-SNAPSHOT")

    implementation("org.lushplugins:ChatColorHandler:5.1.6")
    implementation("org.jetbrains:annotations:26.0.2")
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
        minimize()

        archiveFileName.set("${project.name}-${project.version}.jar")
    }

    processResources{
        expand(project.properties)

        inputs.property("version", rootProject.version)
        filesMatching("settings.properties") {
            expand("version" to rootProject.version)
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = rootProject.group.toString() + ".pluginupdater"
            artifactId = rootProject.name + "-API"
            version = rootProject.version.toString()
            from(project.components["java"])
        }
    }
}
