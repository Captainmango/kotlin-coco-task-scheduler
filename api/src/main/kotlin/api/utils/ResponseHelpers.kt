package api.utils

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.respond
import kotlinx.serialization.*
import kotlinx.serialization.json.JsonObject

suspend inline fun<reified T> ApplicationCall.sendResponse(
    type: String,
    data: List<T> = emptyList(),
    meta: JsonObject = JsonObject(emptyMap()),
    status: HttpStatusCode = HttpStatusCode.OK,
) {
    respond(status, ApiResponse(type=type, data=data, meta=meta, error=null))
}

suspend inline fun ApplicationCall.sendError(
    type: String,
    message: String,
    meta: JsonObject = JsonObject(emptyMap()),
    status: HttpStatusCode = HttpStatusCode.BadRequest,
) {
    respond(status, ApiResponse<Unit>(type=type, data=emptyList(), meta=meta, error=message))
}

@Serializable
data class ApiResponse<T>(
    val type: String,
    val data: List<T> = emptyList(),
    val meta: JsonObject = JsonObject(emptyMap()),
    val error: String? = null
)