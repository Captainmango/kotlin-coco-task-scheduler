package messageQueue

import dev.kourier.amqp.BuiltinExchangeType
import dev.kourier.amqp.channel.AMQPChannel
import dev.kourier.amqp.connection.AMQPConnection
import dev.kourier.amqp.connection.amqpConfig
import dev.kourier.amqp.connection.createAMQPConnection
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalAtomicApi::class)
class RabbitMQConnectionPool(
    val amqpHost: String,
    val poolSize: Int = 10,
    val timeoutSeconds: Duration = 10.seconds,
    val onReady: () -> Unit = {},
) : AutoCloseable {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val closed: AtomicBoolean = AtomicBoolean(false)
    private lateinit var connection: AMQPConnection
    private lateinit var channelPool: Channel<AMQPChannel>

    init {
        scope.launch {
            val conf = amqpConfig(this@RabbitMQConnectionPool.amqpHost, timeout = timeoutSeconds)
            val conn = createAMQPConnection(this, conf)
            this@RabbitMQConnectionPool.connection = conn
            this@RabbitMQConnectionPool.channelPool = Channel(this@RabbitMQConnectionPool.poolSize)

            repeat (this@RabbitMQConnectionPool.poolSize) {
                val chan = conn.openChannel()
                this@RabbitMQConnectionPool.channelPool.send(chan)
            }

            use { aMQPChannel ->
                aMQPChannel.exchangeDeclare(
                    name = "coco",
                    type = BuiltinExchangeType.TOPIC,
                    durable = true,
                    autoDelete = false,
                    internal = false,
                )
            }
        }
    }

    suspend fun getChannel(): AMQPChannel {
        if (this.closed.load()) {
            throw Exception("Ooops, pool is shut.")
        }

        return this.channelPool.receive()
    }

    suspend fun returnChannel(ch: AMQPChannel) {
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
        scope.launch {
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