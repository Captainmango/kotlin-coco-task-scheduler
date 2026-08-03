package messageQueue

import dev.kourier.amqp.AMQPException
import dev.kourier.amqp.channel.AMQPChannel
import dev.kourier.amqp.connection.AMQPConnection
import kotlin.concurrent.atomics.AtomicInt
import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * A pool of RabbitMQ [AMQPChannel]s backed by a single [AMQPConnection]
 * from the Kourier package https://github.com/kourier-amqp/kourier.
 *
 * The pool must be supplied with a [connectionFactory] that creates a new
 * [AMQPConnection] on demand. This factory is invoked once when the pool
 * initializes, inside [coroutineScope], so it should perform any connection
 * establishment required (e.g. opening a TCP connection, negotiating the AMQP
 * handshake) and return a ready-to-use connection. Kourier provides a helper function.
 *
 * [coroutineScope] is the lifecycle owner of the pool. The pool launches its
 * initialisation work in this scope and will automatically call [close] when
 * the scope's [Job] completes or is cancelled. It is expected to remain active
 * for as long as the pool is needed. Closing or cancelling the scope will shut
 * the pool down. Cleanup operations run in an independent internal scope, so
 * the provided scope is never cancelled by the pool itself.
 *
 * Usage example:
 * ```kotlin
 * val pool = RabbitMQConnectionPool(
 *     poolSize = 10,
 *     connectionFactory = { createAMQPConnection() },
 *     coroutineScope = application.coroutineScope,
 * )
 *
 * // Publish a message using a pooled channel
 * pool.use { channel ->
 *     channel.basicPublish("exchange", "routingKey", message)
 * }
 *
 * // Close the pool when the application stops
 * monitor.subscribe(ApplicationStopped) {
 *     pool.close()
 * }
 * ```
 */
@OptIn(ExperimentalAtomicApi::class, DelicateCoroutinesApi::class)
class RabbitMQConnectionPool(
    val poolSize: Int = 10,
    private val connectionFactory: suspend () -> AMQPConnection,
    private val coroutineScope: CoroutineScope,
) : AutoCloseable {

    private sealed class State {
        data class Initialising(val ready: CompletableDeferred<Ready>) : State()

        data class Ready(
            val connection: AMQPConnection,
            val channelPool: Channel<AMQPChannel>,
            val checkedOut: AtomicInt = AtomicInt(0), // Semaphore of channels out. Can safely close at 0
            val allReturned: CompletableDeferred<Unit> = CompletableDeferred(),
        ) : State()

        data object Closed : State()
    }

    private val state = AtomicReference<State>(State.Initialising(CompletableDeferred()))

    // Independent of the provided scope so that cleanup can run even after the
    // provided scope has been cancelled. We never cancel the provided scope,
    // but we do react to its cancellation.
    private val cleanupScope = CoroutineScope(coroutineScope.coroutineContext + SupervisorJob())

    init {
        coroutineScope.launch {
            var connection: AMQPConnection? = null
            var channelPool: Channel<AMQPChannel>? = null
            try {
                connection = connectionFactory()
                channelPool = Channel(poolSize)

                repeat(poolSize) { channelPool.send(connection.openChannel()) }

                val ready = State.Ready(connection, channelPool)
                val current = state.load()
                // Double check current state is initialising then try to exchange.
                // Double check prevents weird state transitions from init to closed.
                if (current is State.Initialising && state.compareAndSet(current, ready)) {
                    current.ready.complete(ready)
                    connection = null
                    channelPool = null
                } else {
                    // close() was called before init finished.
                }
            } catch (e: CancellationException) {
                val current = state.load()
                if (current is State.Initialising && state.compareAndSet(current, State.Closed)) {
                    current.ready.completeExceptionally(
                        IllegalStateException("Pool initialization was cancelled", e)
                    )
                }
                // If we cannot transition normally, something went very wrong here. Should never hit this case.
                throw e
            } catch (e: Throwable) {
                // We can end up here if the connection isn't made or some other external failure happens.
                // We should still be in init state, but if we aren't it doesn't matter as we're still making the pool
                // Finally section does the clean up for us. No need for us to do anything in this block.
                val current = state.load()
                if (current is State.Initialising && state.compareAndSet(current, State.Closed)) {
                    current.ready.completeExceptionally(
                        IllegalStateException("Failed to initialize pool", e)
                    )
                }
            } finally {
                connection?.close()
                channelPool?.close()
                channelPool?.let { pool ->
                    for (channel in pool) {
                        channel.close()
                    }
                }
            }
        }

        // Once the job in the provided scope completes, we should shutdown. Tie this to the
        // lifecycle of the external scope provider/ job.
        coroutineScope.coroutineContext[Job]?.invokeOnCompletion { close() }
    }

    /**
     * Get the State of the pool. Returns Ready state or throws.
     * Will wait for ready state if transitioning from init.
     */
    private suspend fun getReadyState(): State.Ready =
        when (val current = state.load()) {
            is State.Initialising -> {
                try {
                    current.ready.await()
                } catch (e: CancellationException) {
                    throw IllegalStateException("Pool is closed", e)
                }
            }

            is State.Ready -> current
            is State.Closed -> throw IllegalStateException("Pool is closed")
        }

    suspend fun <T> use(block: suspend (AMQPChannel) -> T): T {
        val ready = getReadyState()
        ready.checkedOut.addAndFetch(1)

        val channel =
            try {
                ready.channelPool.receive()
            } catch (e: Throwable) {
                if (ready.checkedOut.addAndFetch(-1) == 0) {
                    ready.allReturned.complete(Unit)
                }
                throw IllegalStateException("Pool is closed or channel receive failed", e)
            }

        return try {
            block(channel)
        } finally {
            withContext(NonCancellable) { returnChannel(ready, channel) }
        }
    }

    private suspend fun returnChannel(ready: State.Ready, channel: AMQPChannel) {
        try {
            // Is unreliable as pool could be closing, which marks this as true
            // but might not have fully transitioned. The else block is a best effort close
            // Finally decrements the semaphore as well as checking all have returned before setting the signal
            if (ready.channelPool.isClosedForSend) {
                channel.close()
            } else {
                try {
                    ready.channelPool.send(channel)
                } catch (e: Throwable) {
                    channel.close()
                }
            }
        } finally {
            if (ready.checkedOut.addAndFetch(-1) == 0) {
                ready.allReturned.complete(Unit)
            }
        }
    }

    override fun close() {
        val current = state.exchange(State.Closed)
        if (current is State.Closed) {
            return
        }

        // Clean up no matter what. Parent scope can be gone, but cleanup will drain and teardown
        cleanupScope.launch { withContext(NonCancellable) { cleanup(current) } }
    }

    private suspend fun cleanup(stateToClean: State) {
        when (stateToClean) {
            is State.Initialising -> {
                stateToClean.ready.cancel()
            }

            is State.Ready -> {
                stateToClean.channelPool.close()
                if (stateToClean.checkedOut.load() == 0) {
                    stateToClean.allReturned.complete(Unit)
                }
                stateToClean.allReturned.await()

                for (channel in stateToClean.channelPool) {
                    try {
                        channel.close()
                    } catch (_: Throwable) {
                        // Best-effort cleanup.
                    }
                }
                try {
                    stateToClean.connection.close()
                } catch (_: Throwable) {
                    // Best-effort cleanup.
                }
            }

            is State.Closed -> Unit
        }
    }
}
