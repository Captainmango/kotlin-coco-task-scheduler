package api.services

import kotlinx.serialization.Serializable
import net.logstash.logback.argument.StructuredArguments.kv
import org.slf4j.LoggerFactory

class InfraService {
    companion object {
        private val logger = LoggerFactory.getLogger(InfraService::class.java)
    }

    fun performHealthcheck(): HealthCheckResponse {
        logger.debug("Performing healthcheck", kv("service", "infra"))
        return HealthCheckResponse("OK").also {
            logger.debug("Healthcheck result", kv("service", "infra"), kv("state", it.state))
        }
    }
}

@Serializable data class HealthCheckResponse(val state: String)
