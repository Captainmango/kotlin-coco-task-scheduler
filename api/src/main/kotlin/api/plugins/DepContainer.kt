package api.plugins

import api.config.envKey
import api.services.InfraService
import api.services.TaskManagementService
import crontab.CrontabManager
import crontab.ICrontabManager
import io.ktor.server.application.Application
import io.ktor.server.plugins.di.dependencies
import parser.CronParser
import java.io.File

fun Application.createDiContainer() {
    val crontabPath = File(attributes[envKey].crontabFile).toPath()

    dependencies {
        provide(InfraService::class)
        provide<ICrontabManager> { CrontabManager(crontabPath) }
        provide(TaskManagementService::class)
    }
}