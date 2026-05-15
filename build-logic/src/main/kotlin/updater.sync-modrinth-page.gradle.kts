plugins {
    id("updater.modrinth-base")
}

modrinth {
    syncBodyFrom.set(rootProject.file("README.md").readText())
}