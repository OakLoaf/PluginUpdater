plugins {
    id("updater.modrinth-base")
}

modrinth {
    if (System.getenv("RELEASE_TYPE") == "release") {
        versionNumber.set(rootProject.version.toString())
        changelog.set(rootProject.getChangelogSinceLastTag())
    } else {
        versionNumber.set("${rootProject.version}-${rootProject.getCurrentCommitHash()}")
    }
    uploadFile.set(file("build/libs/${project.name}-${project.version}.jar"))
    versionType.set(System.getenv("RELEASE_TYPE"))
    gameVersions.addAll(
        "1.21.4", "1.21.5", "1.21.6", "1.21.7", "1.21.8", "1.21.9", "1.21.10", "1.21.11",
        "21.6"
    )
}

tasks.modrinth {
    dependsOn("shadowJar")
}