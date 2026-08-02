package api.config

import api.services.HealthCheckResponse
import api.services.InfraService
import api.services.TaskManagementService
import api.utils.sendResponse
import io.ktor.server.application.*
import io.ktor.server.plugins.di.dependencies
import io.ktor.server.routing.*

fun Application.routeConfig() {
    routing {
        route("api/v1") {
            get("/health") {
                val srv = dependencies.resolve<InfraService>()
                val healthCheckResponse = srv.performHealthcheck()
                call.sendResponse<HealthCheckResponse>("healthcheck", listOf(healthCheckResponse))
            }

            route("/tasks") {
                get {
                    val srv =  dependencies.resolve<TaskManagementService>()
                    val results = srv.listTasks()
                    call.sendResponse("task", results)
                }
            }
        }
    }
}