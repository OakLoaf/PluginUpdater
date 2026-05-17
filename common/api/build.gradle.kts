plugins {
    id("net.kyori.blossom")
    id("org.jetbrains.gradle.plugin.idea-ext")
}

sourceSets {
    main {
        blossom {
            javaSources {
                property("version", project.version.toString())
            }
        }
    }
}

dependencies {
    api("com.google.guava:guava:33.6.0-jre")
    api("com.google.code.gson:gson:2.14.0")
    compileOnlyApi("org.jetbrains:annotations:26.1.0")
}