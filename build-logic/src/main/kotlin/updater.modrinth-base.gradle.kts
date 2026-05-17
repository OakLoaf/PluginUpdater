plugins {
    id("com.modrinth.minotaur")
}

modrinth {
    token.set(System.getenv("MODRINTH_TOKEN"))
    projectId.set("IBSpJfbm")
}