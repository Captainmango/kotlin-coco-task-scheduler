package api.config

import api.utils.sendResponse
import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.routeConfig() {
    routing {
        route("api/v1") {
            get("/health") {
                call.sendResponse<Unit>("healthcheck")
            }
        }
    }
}