plugins {
    id("com.gradleup.shadow")
    id("xyz.jpenilla.run-paper")
    id("updater.publish-modrinth")
}

dependencies {
    compileOnly("com.velocitypowered:velocity-api:3.5.0-SNAPSHOT")

    implementation(project(":common:impl"))
    implementation(project(":platform:velocity-api"))
    implementation("io.github.revxrsal:lamp.velocity:4.0.0-rc.16")
    implementation("io.github.revxrsal:lamp.brigadier:4.0.0-rc.16")
}

modrinth {
    loaders.addAll("velocity")
}