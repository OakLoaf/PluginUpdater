plugins {
    `java-library`
    `maven-publish`
    id("updater.build-logic")
    id("updater.sync-modrinth-page")
}

group = "org.lushplugins"
version = "2.2.0"

allprojects {
    plugins.apply("java-library")
    plugins.apply("maven-publish")

    group = rootProject.group
    version = rootProject.version

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