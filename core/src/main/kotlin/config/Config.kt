package config

import io.github.cdimascio.dotenv.Dotenv
import io.github.cdimascio.dotenv.dotenv

class Config (private val dotenv: Dotenv = dotenv { ignoreIfMissing = true }) {
    val crontabFile: String = dotenv.get("CRONTAB_FILE", "")
}