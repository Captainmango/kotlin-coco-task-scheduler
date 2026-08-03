package messageQueue

import dev.kourier.amqp.channel.AMQPChannel
import dev.kourier.amqp.connection.AMQPConnection
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class RabbitMQConnectionPoolTest {
    @Test
    fun testUseReturnsTheResultOfTheBlock() = runTest {
        val channel = mockk<AMQPChannel>(relaxed = true)
        val connection =
            mockk<AMQPConnection>(relaxed = true) { coEvery { openChannel() } returns channel }

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

    @Test
    fun testCloseWaitsForCheckedOutChannels() = runTest {
        val channel = mockk<AMQPChannel>(relaxed = true)
        val connection =
            mockk<AMQPConnection>(relaxed = true) { coEvery { openChannel() } returns channel }

        val pool =
            RabbitMQConnectionPool(
                poolSize = 1,
                connectionFactory = { connection },
                coroutineScope = this,
            )

        advanceUntilIdle()

        var blockCompleted = false
        val result = async {
            pool.use {
                delay(1_000)
                blockCompleted = true
                "hello"
            }
        }

        advanceTimeBy(500)
        assertFalse(blockCompleted)

        pool.close()
        advanceUntilIdle()

        assertTrue(blockCompleted)
        assertEquals("hello", result.await())
        coVerify { connection.close() }
    }

    @Test
    fun testScopeCancellationClosesPool() = runTest {
        val channel = mockk<AMQPChannel>(relaxed = true)
        val connection =
            mockk<AMQPConnection>(relaxed = true) { coEvery { openChannel() } returns channel }

        val scope = CoroutineScope(this.coroutineContext.minusKey(Job) + SupervisorJob())
        val pool =
            RabbitMQConnectionPool(
                poolSize = 1,
                connectionFactory = { connection },
                coroutineScope = scope,
            )

        advanceUntilIdle()

        scope.cancel()
        advanceUntilIdle()

        coVerify { connection.close() }

        pool.close()
        advanceUntilIdle()
    }

    @Test
    fun testUseThrowsAfterClose() = runTest {
        val channel = mockk<AMQPChannel>(relaxed = true)
        val connection =
            mockk<AMQPConnection>(relaxed = true) { coEvery { openChannel() } returns channel }

        val pool =
            RabbitMQConnectionPool(
                poolSize = 1,
                connectionFactory = { connection },
                coroutineScope = this,
            )

        advanceUntilIdle()
        pool.close()
        advanceUntilIdle()

        assertFailsWith<IllegalStateException> { pool.use { "hello" } }
    }
}
