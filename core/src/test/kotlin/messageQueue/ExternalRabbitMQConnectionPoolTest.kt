package messageQueue

import config.basePath
import dev.kourier.amqp.AMQPResponse
import dev.kourier.amqp.BuiltinExchangeType
import dev.kourier.amqp.connection.createAMQPConnection
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.testcontainers.containers.ComposeContainer
import org.testcontainers.containers.wait.strategy.DockerHealthcheckWaitStrategy
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.io.File
import kotlin.test.Test


@Testcontainers
class ExternalRabbitMQConnectionPoolTest {
    companion object {
        private lateinit var connectionPool: RabbitMQConnectionPool
        private const val SERVICE_NAME = "rabbitmq"
        private const val SERVICE_PORT = 5672

        @Container
        val composeStack: ComposeContainer = ComposeContainer(
            File(basePath().resolve("docker-compose.test.yml").toString())
        )
            .waitingFor(SERVICE_NAME, DockerHealthcheckWaitStrategy())
            .withExposedService(SERVICE_NAME, SERVICE_PORT)

        // Top scope we pass to pool. Allows for tidy clean up by cancelling and awaiting cleanup func
        val lifecycleScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

        @JvmStatic
        @BeforeAll
        fun setUp(): Unit {
            runBlocking {
                val host = this@Companion.composeStack.getServiceHost(SERVICE_NAME, SERVICE_PORT)
                val port = this@Companion.composeStack.getServicePort(SERVICE_NAME, SERVICE_PORT)
                this@Companion.connectionPool = RabbitMQConnectionPool(
                    connectionFactory = {
                        createAMQPConnection(
                            this@Companion.lifecycleScope,
                            "amqp://$host:$port/"
                        )
                    },
                    coroutineScope = this@Companion.lifecycleScope
                )
            }
        }

        @JvmStatic
        @AfterAll
        fun tearDown(): Unit {
            if (::connectionPool.isInitialized)
                connectionPool.close()

            lifecycleScope.cancel()
            runBlocking {
                lifecycleScope.coroutineContext[Job]?.join()
            }

        }
    }

    @Test
    fun testItDoesSomething() {
        runBlocking {
            val exchangeState = connectionPool.use { ch ->
                ch.exchangeDeclare(
                    "test-exchange",
                    BuiltinExchangeType.TOPIC,
                )
            }

            Assertions.assertEquals(AMQPResponse.Channel.Exchange.Declared, exchangeState)
        }
    }
}