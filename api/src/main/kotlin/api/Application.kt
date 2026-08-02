package api

import api.config.routeConfig
import api.plugins.createDiContainer
import api.plugins.jsonSerialisation
import io.ktor.server.application.*

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

fun Application.module() {
    createDiContainer()
    jsonSerialisation()
    routeConfig()
}