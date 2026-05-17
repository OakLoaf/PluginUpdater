import org.gradle.api.Project
import java.io.BufferedReader
import java.io.InputStreamReader

fun Project.getCurrentCommitHash(): String {
    val process = ProcessBuilder("git", "rev-parse", "--short", "HEAD").start()
    val reader = BufferedReader(InputStreamReader(process.inputStream))
    val commitHash = reader.readLine()
    reader.close()
    process.waitFor()
    if (process.exitValue() == 0) {
        return commitHash ?: ""
    } else {
        throw IllegalStateException("Failed to retrieve the commit hash.")
    }
}

fun Project.getLastTag(): String {
    return ProcessBuilder("git", "describe", "--tags", "--abbrev=0")
        .start().inputStream.bufferedReader().readText().trim()
}

fun Project.getChangelogSinceLastTag(): String {
    return ProcessBuilder("git", "log", "${getLastTag()}..HEAD", "--pretty=format:* %s ([#%h](https://github.com/OakLoaf/PluginUpdater/commit/%H))")
        .start().inputStream.bufferedReader().readText().trim()
}