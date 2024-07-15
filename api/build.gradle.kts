plugins {
    `java-library`
    id("com.github.johnrengelman.shadow") version("8.1.1")
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.20.6-R0.1-SNAPSHOT")

    implementation("org.lushplugins:ChatColorHandler:3.1.0")
    implementation("org.jetbrains:annotations:24.0.0")
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
        filesMatching("properties.yml") {
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