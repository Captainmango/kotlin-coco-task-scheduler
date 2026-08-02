package api.config

import api.module
import api.services.HealthCheckResponse
import api.utils.ApiResponse
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.junit.jupiter.api.Assertions
import kotlin.test.Test

class RouteTest {

    @Test
    fun `health endpoint responds OK`(): Unit = testApplication {
        application {
            module()
        }

        val result = client.get("api/v1/health")

        Assertions.assertEquals(HttpStatusCode.OK, result.status)
        val jsonResult = Json.decodeFromString<ApiResponse<JsonObject>>(result.bodyAsText())

        val expectedData = JsonObject(mapOf("state" to JsonPrimitive("OK")))

        Assertions.assertEquals("healthcheck", jsonResult.type)
        Assertions.assertEquals(listOf(expectedData), jsonResult.data)
        Assertions.assertEquals(JsonObject(emptyMap()), jsonResult.meta)
        Assertions.assertEquals(null, jsonResult.error)
    }
}