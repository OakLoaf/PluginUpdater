dependencies {
    compileOnly("org.geysermc.geyser:core:2.11.3-SNAPSHOT")
    compileOnly(project(":common:api"))

    api("net.kyori:adventure-text-minimessage:5.2.0")
    api("net.kyori:adventure-text-serializer-plain:5.2.0")
}