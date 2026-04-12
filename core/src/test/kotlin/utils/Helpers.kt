package utils

import crontab.CrontabManager
import io.github.cdimascio.dotenv.Dotenv
import java.nio.file.Files
import java.nio.file.StandardOpenOption

/**
 * Helper to make testing Config class simpler and deterministic
 */
fun fakeEnv(vars: Map<String, String>) = object : Dotenv {
    override fun entries() = emptySet<io.github.cdimascio.dotenv.DotenvEntry>()
    override fun entries(filter: Dotenv.Filter) = emptySet<io.github.cdimascio.dotenv.DotenvEntry>()
    override fun get(key: String): String? = vars[key]
    override fun get(key: String, defaultValue: String): String = vars[key] ?: defaultValue
}

/**
 * Helper to create a crontab manager instance with a temp file. Used in tests only (NOT THREAD SAFE)
 */
fun createCrontabManager(content: String = ""): Pair<CrontabManager, java.nio.file.Path> {
    val file = Files.createTempFile("crontab-test-", ".txt")
    Files.writeString(file, content)

    val reader = Files.newBufferedReader(file)
    val writer = Files.newBufferedWriter(file, StandardOpenOption.SYNC)
    return Pair(CrontabManager(reader, writer), file)
}
