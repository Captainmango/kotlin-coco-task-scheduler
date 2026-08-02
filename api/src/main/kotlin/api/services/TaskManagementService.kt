package api.services

import api.errors.CocoBingoError
import crontab.CronEntry
import crontab.ICrontabManager
import java.util.UUID
import kotlinx.serialization.Serializable
import parser.CronParser

class TaskManagementService(private val crontabManager: ICrontabManager) {
    fun listTasks(): List<CrontabTask> {
        return crontabManager.list().map { mapCronEntryToTask(it) }
    }

    fun addTask(task: CommandTask): CrontabTask {
        val cronParser = CronParser.make(task.cron)

        try {
            val id = UUID.randomUUID()
            cronParser.input = task.cron

            val cronEntry = CronEntry(id = id, cron = cronParser.parse(), cmd = task.cmd)

            crontabManager.add(listOf(cronEntry))

            return mapCronEntryToTask(cronEntry)
        } catch (e: Throwable) {
            throw e
        }
    }

    fun getById(id: String): CrontabTask {
        try {
            val uuid = UUID.fromString(id)
            val ce = crontabManager.find(uuid)
            return mapCronEntryToTask(ce)
        } catch (ne: NoSuchElementException) {
            throw CocoBingoError.CronTaskNotFound(
                "Unable to find cron task with ID: ${id}",
                cause = ne,
            )
        } catch (e: Throwable) {
            throw CocoBingoError.GenericFailure("Encountered error", cause = e)
        }
    }

    fun deleteById(id: String) {
        val uuid = UUID.fromString(id)
        crontabManager.delete(uuid)
    }

    private fun mapCronEntryToTask(cte: CronEntry): CrontabTask =
        CrontabTask(cte.id.toString(), cte.cron.toString(), cte.cmd)
}

@Serializable data class CrontabTask(val id: String, val cron: String, val cmd: String)

@Serializable data class CommandTask(val cron: String, val cmd: String)
