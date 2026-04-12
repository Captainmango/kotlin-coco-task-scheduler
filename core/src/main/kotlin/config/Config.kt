package config

import io.github.cdimascio.dotenv.Dotenv
import io.github.cdimascio.dotenv.dotenv
import java.io.File

class Config (private val dotenv: Dotenv = createDotenv()) {
    val crontabFile: String = dotenv.get("CRONTAB_FILE", "")
}

var config: Config? = null
    get() {
        if (field == null) field = Config()
        return field
    }
    private set

private fun createDotenv(): Dotenv {
    val projectRoot = findProjectRoot()
    return dotenv {
        directory = projectRoot.absolutePath
        ignoreIfMissing = true
    }
}

private fun findProjectRoot(): File {
    var dir: File? = File(System.getProperty("user.dir"))

    // Walk up the directory tree looking for .env file
    while (dir != null) {
        val envFile = File(dir, ".env")
        if (envFile.exists()) {
            return dir
        }
        dir = dir.parentFile
    }

    // Fallback to current directory if not found
    return File(System.getProperty("user.dir"))
}