package api

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.application.Application
import io.ktor.server.testing.*
import kotlin.test.*

class ApplicationTest {
    @Test
    fun `it starts with hello world`(): Unit = testApplication {
        val fn: (ap: Application) -> Unit = { ap ->
            ap.module()
        }

        application(fn)

        val response = client.get("/")

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("Hello, World!", response.bodyAsText(Charsets.UTF_8))
    }
}