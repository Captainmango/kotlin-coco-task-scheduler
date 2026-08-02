package api.services

import crontab.ICrontabManager
import kotlinx.serialization.Serializable

class TaskManagementService(
    private val crontabManager: ICrontabManager
) {
    fun listTasks(): List<Task> {
        return crontabManager.list().map { cte -> Task(cte.id.toString()) }
    }
}

@Serializable
data class Task(
    val id: String,
)