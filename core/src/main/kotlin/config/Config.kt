package config

import io.github.cdimascio.dotenv.Dotenv
import io.github.cdimascio.dotenv.dotenv
import java.io.File

class Config (private val dotenv: Dotenv = createDotenv()) {
    val crontabFile: String = dotenv.get("CRONTAB_FILE", "$projectRoot/e2e/test.crontab")
}

var config: Config? = null
    get() {
        if (field == null) field = Config()
        return field
    }
    private set

fun basePath(): File = File(projectRoot)

val projectRoot: String by lazy {
    Config::class.java.classLoader
        .getResourceAsStream("project-info.properties")
        ?.use { stream ->
            val props = java.util.Properties()
            props.load(stream)
            props.getProperty("project.root")
        }
        ?: throw IllegalStateException("project-info.properties not found. Did you run the build?")
}

private fun createDotenv(): Dotenv {
    return dotenv {
        directory = basePath().absolutePath
        ignoreIfMissing = true
    }
}