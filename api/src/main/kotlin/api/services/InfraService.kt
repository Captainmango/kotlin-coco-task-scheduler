package api.services

import kotlinx.serialization.Serializable

class InfraService {
    fun performHealthcheck(): HealthCheckResponse = HealthCheckResponse("OK")
}

@Serializable
data class HealthCheckResponse(
    val state: String
)