plugins {
    id("com.gradleup.shadow") version("9.3.1")
}

subprojects {
    apply(plugin = "com.gradleup.shadow")

    tasks {
        shadowJar {
            minimize()

            archiveFileName.set("${project.name}-${project.version}.jar")
        }
    }

    publishing {
        publications {
            create<MavenPublication>("maven") {
                groupId = rootProject.group.toString()
                artifactId = rootProject.name + "-" + project.name
                version = rootProject.version.toString()
                from(project.components["java"])
            }
        }
    }
}