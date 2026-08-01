dependencies {
    api(project(":common:impl"))
    implementation("io.github.revxrsal:lamp.cli:4.0.0-rc.17")
    api("net.kyori:adventure-text-minimessage:5.2.0")
    api("com.electronwill.night-config:json:3.9.0")
}

tasks{
    jar {
        archiveFileName.set("${rootProject.name}-cli-${project.version}.jar")

        manifest.attributes (
            "Main-Class" to "org.lushplugins.pluginupdater.cli.PluginUpdaterCLI"
        )
    }
}