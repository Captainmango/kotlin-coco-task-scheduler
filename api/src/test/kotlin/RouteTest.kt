import api.module
import api.services.CrontabTask
import api.utils.ApiResponse
import crontab.CrontabManager
import crontab.ICrontabManager
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.server.plugins.di.dependencies
import io.ktor.server.testing.testApplication
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.io.path.writeText
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

    @Test
    fun `list tasks endpoint returns results`(
        @TempDir tmpPath: Path
    ): Unit = testApplication {
        val crontabPath = tmpPath.resolve("test.crontab")
        val expectedId = "11111111-2222-3333-4444-555555555555"
        crontabPath.writeText("1-5 1-5 1-5 * * root /app/fake-task 2>&1 | tee -a /tmp/log # ${expectedId} \\n")

        application {
            dependencies {
                provide<ICrontabManager> { CrontabManager(crontabPath) }
            }
            module()
        }

        val result = client.get("api/v1/tasks")
        Assertions.assertEquals(HttpStatusCode.OK, result.status)

        val resultJson = Json.decodeFromString<ApiResponse<CrontabTask>>(result.bodyAsText())

        Assertions.assertEquals(1, resultJson.data.count())
        Assertions.assertEquals(expectedId, resultJson.data[0].id)
    }

    @Test
    fun `list tasks endpoint returns no results when file is empty`(
        @TempDir tmpPath: Path
    ): Unit = testApplication {
        val crontabPath = tmpPath.resolve("test.crontab")
        crontabPath.writeText("")

        application {
            dependencies {
                provide<ICrontabManager> { CrontabManager(crontabPath) }
            }
            module()
        }

        val result = client.get("api/v1/tasks")

        Assertions.assertEquals(HttpStatusCode.OK, result.status)

        val resultJson = Json.decodeFromString<ApiResponse<CrontabTask>>(result.bodyAsText())
        Assertions.assertEquals(0, resultJson.data.count())
    }
}