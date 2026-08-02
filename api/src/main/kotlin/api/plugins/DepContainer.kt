package api.plugins

import api.services.InfraService
import io.ktor.server.application.Application
import io.ktor.server.plugins.di.dependencies

fun Application.createDiContainer() {
    dependencies {
        provide(InfraService::class)
    }
}