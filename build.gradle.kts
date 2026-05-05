plugins {
    `java-library`
    `maven-publish`
    id("updater.build-logic")
    id("updater.sync-modrinth-page")
}

group = "org.lushplugins"
version = "3.0.0"

allprojects {
    plugins.apply("java-library")
    plugins.apply("maven-publish")

    group = rootProject.group
    version = rootProject.version

    dependencies {
        components.all {
            withVariant("apiElements") {
                attributes {
                    attribute(TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE, 21)
                }
            }
            withVariant("runtimeElements") {
                attributes {
                    attribute(TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE, 21)
                }
            }
        }
    }

    java {
        // Ensures all code is written against Java 21
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21

        // Allows compileOnly dependencies that require Java 25
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(25))
        }

        withSourcesJar()
    }

    tasks {
        withType<JavaCompile> {
            options.encoding = "UTF-8"

            // Ensures the output jar is compatible with Java 21
            options.release.set(21)
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