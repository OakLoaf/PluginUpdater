subprojects {
    publishing {
        publications {
            create<MavenPublication>("maven") {
                groupId = rootProject.group.toString() + "." + rootProject.name.lowercase()
                artifactId = "updater." + project.name
                version = rootProject.version.toString()
                from(project.components["java"])
            }
        }
    }
}