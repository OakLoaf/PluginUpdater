plugins {
    id("net.kyori.blossom")
    id("org.jetbrains.gradle.plugin.idea-ext")
}

sourceSets {
    main {
        blossom {
            javaSources {
                property("version", project.version.toString())
                property("commit", rootProject.getCurrentCommitHash())
            }
        }
    }
}

dependencies {
    api("com.google.guava:guava:33.6.0-jre")
    api("com.google.code.gson:gson:2.14.0")
    api("net.kyori:adventure-text-logger-slf4j:5.2.0")
    api("org.slf4j:slf4j-api:2.0.17")
    compileOnlyApi("org.jetbrains:annotations:26.1.0")
}