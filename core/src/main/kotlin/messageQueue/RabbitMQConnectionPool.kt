package messageQueue

import dev.kourier.amqp.channel.AMQPChannel
import dev.kourier.amqp.connection.AMQPConnection
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.ExperimentalAtomicApi

@OptIn(ExperimentalAtomicApi::class)
class RabbitMQConnectionPool(
    val poolSize: Int = 10,
    private val connectionFactory: suspend () -> AMQPConnection,
    private val coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO),
) : AutoCloseable {
    private val closed: AtomicBoolean = AtomicBoolean(false)
    private lateinit var connection: AMQPConnection
    private lateinit var channelPool: Channel<AMQPChannel>

    init {
        this@RabbitMQConnectionPool.coroutineScope.launch {
            val conn = connectionFactory()
            this@RabbitMQConnectionPool.connection = conn
            this@RabbitMQConnectionPool.channelPool = Channel(this@RabbitMQConnectionPool.poolSize)

            repeat (this@RabbitMQConnectionPool.poolSize) {
                val chan = conn.openChannel()
                this@RabbitMQConnectionPool.channelPool.send(chan)
            }
        }
    }

    private suspend fun getChannel(): AMQPChannel {
        if (this.closed.load()) {
            throw Exception("Ooops, pool is shut.")
        }

        return this.channelPool.receive()
    }

    private suspend fun returnChannel(ch: AMQPChannel) {
        if (this.closed.load()) {
            ch.close()
            return
        }

        this.channelPool.send(ch)
    }

    suspend fun <R> use(block: suspend (AMQPChannel) -> R): R {
        val channel = this.getChannel()

        return try {
            block(channel)
        } finally {
            withContext(NonCancellable) {
                this@RabbitMQConnectionPool.returnChannel(channel)
            }
        }
    }

    override fun close() {
        this@RabbitMQConnectionPool.coroutineScope.launch {
            if (this@RabbitMQConnectionPool.closed.load()) {
                // Spinloop as we already closed the pool
                return@launch
            }

            // Close the pool
            this@RabbitMQConnectionPool.closed.exchange(true)

            // Drain the pool
            this@RabbitMQConnectionPool.channelPool.close()
            for (c in this@RabbitMQConnectionPool.channelPool) {
                c.close()
            }

            // Close the connection down
            this@RabbitMQConnectionPool.connection.close()
        }
    }
}