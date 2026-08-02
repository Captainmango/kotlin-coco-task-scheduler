package api.errors

sealed class CocoBingoError(
    override val message: String = "",
    override val cause: Throwable? = null,
) : Exception(message, cause) {
    class GenericFailure(
        override val message: String,
        override val cause: Throwable? = null,
    ): CocoBingoError(message, cause)

    class CronTaskNotFound(
        override val message: String = "Unable to find cron task.",
        override val cause: Throwable? = null
    ) : CocoBingoError(message, cause)
}