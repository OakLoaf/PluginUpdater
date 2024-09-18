plugins {
    `java-library`
    `maven-publish`
    id("com.github.johnrengelman.shadow") version("8.1.1")
    id("com.modrinth.minotaur") version("2.+")
}

allprojects {
    apply(plugin = "maven-publish")

    group = "org.lushplugins"
    version = "1.0.1"

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
    compileOnly("org.spigotmc:spigot-api:1.20.6-R0.1-SNAPSHOT")

    implementation(project(":api"))

    implementation("org.lushplugins:LushLib:0.1.7.7")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))

    withSourcesJar()
}

tasks {
    withType<JavaCompile> {
        options.encoding = "UTF-8"
    }

    register("printVersionName") {
        doLast {
            println(project.version)
        }
    }


    shadowJar {
        relocate("org.lushplugins.lushlib", "org.lushplugins.pluginupdater.libraries.lushlib")

        minimize()

        val folder = System.getenv("pluginFolder")
        if (folder != null) destinationDirectory.set(file(folder))
        archiveFileName.set("${project.name}-${project.version}.jar")
    }

    processResources{
        expand(project.properties)

        inputs.property("version", rootProject.version)
        filesMatching("plugin.yml") {
            expand("version" to rootProject.version)
        }
    }
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
    versionNumber.set(rootProject.version.toString())
    uploadFile.set(file("build/libs/${project.name}-${project.version}.jar"))
    versionType.set("release")
    gameVersions.addAll(
        "1.18", "1.18.1", "1.18.2",
        "1.19", "1.19.1", "1.19.2", "1.19.3", "1.19.4",
        "1.20", "1.20.1", "1.20.2", "1.20.3", "1.20.4", "1.20.5", "1.20.6",
        "1.21", "1.21.1"
    )
    loaders.addAll("spigot", "paper", "purpur")
    syncBodyFrom.set(rootProject.file("README.md").readText())
}
//