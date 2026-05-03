plugins {
    `java-library`
    `maven-publish`
}

group = "org.lushplugins"
version = "2.2.0"

allprojects {
    apply(plugin = "java-library")
    apply(plugin = "maven-publish")

    group = rootProject.group
    version = rootProject.version

    repositories {
        mavenLocal()
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/") // Paper
        maven("https://repo.lushplugins.org/snapshots") // ChatColorHandler
    }

    java {
        toolchain.languageVersion.set(JavaLanguageVersion.of(21))

        withSourcesJar()
    }

    tasks {
        withType<JavaCompile> {
            options.encoding = "UTF-8"
        }
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
