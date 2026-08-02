package api.services

import api.errors.CocoBingoError
import crontab.CronEntry
import crontab.ICrontabManager
import java.util.UUID
import kotlinx.serialization.Serializable
import net.logstash.logback.argument.StructuredArguments.kv
import org.slf4j.LoggerFactory
import parser.CronParser

class TaskManagementService(private val crontabManager: ICrontabManager) {
    companion object {
        private val logger = LoggerFactory.getLogger(TaskManagementService::class.java)
    }

    fun listTasks(): List<CrontabTask> {
        logger.info("Listing all cron tasks")
        return crontabManager
            .list()
            .map { mapCronEntryToTask(it) }
            .also { logger.info("Found {} cron task(s)", kv("taskCount", it.size)) }
    }

    fun addTask(task: CommandTask): CrontabTask {
        logger.info("Adding new cron task", kv("cron", task.cron), kv("cmd", task.cmd))
        val cronParser = CronParser.make(task.cron)

        try {
            val id = UUID.randomUUID()
            cronParser.input = task.cron

            val cronEntry = CronEntry(id = id, cron = cronParser.parse(), cmd = task.cmd)

            crontabManager.add(listOf(cronEntry))

            return mapCronEntryToTask(cronEntry).also {
                logger.info(
                    "Added cron task",
                    kv("taskId", it.id),
                    kv("cron", task.cron),
                    kv("cmd", task.cmd),
                )
            }
        } catch (e: Throwable) {
            logger.error("Failed to add cron task", kv("cron", task.cron), kv("cmd", task.cmd), e)
            throw e
        }
    }

    fun getById(id: String): CrontabTask {
        logger.info("Fetching cron task", kv("taskId", id))
        try {
            val uuid = UUID.fromString(id)
            val ce = crontabManager.find(uuid)
            return mapCronEntryToTask(ce).also { logger.info("Found cron task", kv("taskId", id)) }
        } catch (ne: NoSuchElementException) {
            logger.warn("Cron task not found", kv("taskId", id))
            throw CocoBingoError.CronTaskNotFound(
                "Unable to find cron task with ID: ${id}",
                cause = ne,
            )
        } catch (e: Throwable) {
            logger.error("Error fetching cron task", kv("taskId", id), e)
            throw CocoBingoError.GenericFailure("Encountered error", cause = e)
        }
    }

    fun deleteById(id: String) {
        logger.info("Deleting cron task", kv("taskId", id))
        val uuid = UUID.fromString(id)
        crontabManager.delete(uuid)
        logger.info("Deleted cron task", kv("taskId", id))
    }

    private fun mapCronEntryToTask(cte: CronEntry): CrontabTask =
        CrontabTask(cte.id.toString(), cte.cron.toString(), cte.cmd)
}

@Serializable data class CrontabTask(val id: String, val cron: String, val cmd: String)

@Serializable data class CommandTask(val cron: String, val cmd: String)
