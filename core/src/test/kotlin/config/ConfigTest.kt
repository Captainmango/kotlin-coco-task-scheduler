package config

import io.github.cdimascio.dotenv.Dotenv
import kotlin.test.Test
import kotlin.test.assertEquals

class ConfigTest {
    /**
     * Helper to make testing Config class simpler and deterministic
     */
    private fun fakeEnv(vars: Map<String, String>) = object : Dotenv {
        override fun entries() = emptySet<io.github.cdimascio.dotenv.DotenvEntry>()
        override fun entries(filter: Dotenv.Filter) = emptySet<io.github.cdimascio.dotenv.DotenvEntry>()
        override fun get(key: String): String? = vars[key]
        override fun get(key: String, defaultValue: String): String = vars[key] ?: defaultValue
    }

    @Test
    fun `loads CRONTAB_FILE`() {
        val config = Config(fakeEnv(mapOf("CRONTAB_FILE" to "/etc/crontab")))
        assertEquals("/etc/crontab", config.crontabFile)
    }
}