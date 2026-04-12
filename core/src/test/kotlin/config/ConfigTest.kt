package config

import kotlin.test.Test
import kotlin.test.assertEquals
import utils.fakeEnv

class ConfigTest {
    @Test
    fun testItLoadsConfig() {
        val config = Config(fakeEnv(mapOf("CRONTAB_FILE" to "/etc/crontab")))
        assertEquals("/etc/crontab", config.crontabFile)
    }
}
