package messageQueue

import dev.kourier.amqp.channel.AMQPChannel
import dev.kourier.amqp.connection.AMQPConnection
import io.mockk.coEvery
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class RabbitMQConnectionPoolTest {
    @Test
    fun testUseReturnsTheResultOfTheBlock() = runTest {
        val channel = mockk<AMQPChannel>(relaxed = true)
        val connection = mockk<AMQPConnection>(relaxed = true) {
            coEvery { openChannel() } returns channel
        }

        val pool =
            RabbitMQConnectionPool(
                poolSize = 1,
                connectionFactory = { connection },
                coroutineScope = this,
            )

        advanceUntilIdle()

        val result = pool.use { "hello" }

        assertEquals("hello", result)

        pool.close()
        advanceUntilIdle()
    }
}
