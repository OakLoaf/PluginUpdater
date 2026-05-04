plugins {
    id("com.gradleup.shadow")
    id("com.modrinth.minotaur")
    id("xyz.jpenilla.run-paper")
}

dependencies {
    compileOnly("com.velocitypowered:velocity-api:3.5.0-SNAPSHOT")

    implementation(project(":common:impl"))
    implementation(project(":platform:velocity-api"))
    implementation("io.github.revxrsal:lamp.velocity:4.0.0-rc.16")
}