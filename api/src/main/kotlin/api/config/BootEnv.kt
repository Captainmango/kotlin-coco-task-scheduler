package api.config

import api.utils.Env
import api.utils.env
import io.ktor.server.application.Application
import io.ktor.util.AttributeKey

val envKey = AttributeKey<Env>("Env")

fun Application.bootEnv() {
    val env: Env = env!!
    attributes[envKey] = env
}