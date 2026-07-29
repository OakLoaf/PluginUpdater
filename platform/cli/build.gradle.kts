dependencies {
    implementation(project(":common:impl"))
    implementation("io.github.revxrsal:lamp.cli:4.0.0-rc.17")
//    implementation("net.kyori:adventure-api:5.2.0") // TODO: Check whether this dependency is needed in addition to MiniMessage
    implementation("net.kyori:adventure-text-minimessage:5.2.0")
    api("com.electronwill.night-config:json:3.9.0")
}