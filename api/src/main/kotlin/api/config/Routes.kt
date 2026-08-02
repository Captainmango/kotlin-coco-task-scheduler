package api.config

import api.errors.CocoBingoError
import api.services.CommandTask
import api.services.HealthCheckResponse
import api.services.InfraService
import api.services.TaskManagementService
import api.utils.sendError
import api.utils.sendResponse
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.plugins.di.dependencies
import io.ktor.server.request.receive
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
                val srv: TaskManagementService by dependencies

                get {
                    val results = srv.listTasks()
                    call.sendResponse("task", results)
                }

                post {
                    val payload = call.receive<CommandTask>()
                    val result = srv.addTask(payload)
                    call.sendResponse("task", listOf(result))
                }

                get("/{id}") {
                    val idParam = call.parameters["id"].toString()

                    try {
                        val result = srv.getById(idParam)
                        call.sendResponse("task", listOf(result))
                    } catch (ce: CocoBingoError) {
                        when (ce) {
                            is CocoBingoError.CronTaskNotFound ->
                                call.sendError(
                                    "task-not-found",
                                    ce.message,
                                    status = HttpStatusCode.NotFound,
                                )
                            is CocoBingoError.GenericFailure ->
                                call.sendError(
                                    "generic-failure",
                                    ce.message,
                                    status = HttpStatusCode.InternalServerError,
                                )
                        }
                    }
                }
                delete("/{id}") {
                    val idParam = call.parameters["id"].toString()
                    srv.deleteById(idParam)
                    call.sendResponse<Unit>("task", status = HttpStatusCode.NoContent)
                }
            }
        }
    }
}
