package mobi.sevenwinds

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.defaultheaders.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import mobi.sevenwinds.app.Config
import mobi.sevenwinds.app.budget.configureBudgetRoutes
import mobi.sevenwinds.modules.*
import org.slf4j.LoggerFactory
import org.slf4j.event.Level

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

private suspend fun ApplicationCall.respondError(status: HttpStatusCode, message: String) {
    this.respond(status, ErrorResponse(error = message))
}

fun Application.module() {
    val config = Config(this)
    DatabaseFactory.init(environment.config)

    configureBudgetRoutes()

    initSwagger() // Настройка Swagger

    install(DefaultHeaders)

    install(ContentNegotiation) {
        jackson {
            enable(SerializationFeature.INDENT_OUTPUT)
            configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            registerModule(Jdk8Module())
        }
    }

    install(CallLogging) {
        level = Level.INFO
        filter { call -> config.logAllRequests || call.request.path().startsWith("/") }
    }

    install(CORS) {
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Patch)
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)
        allowCredentials = true
        anyHost() // В продакшене замените на разрешённые хосты
    }

    configureRouting() // Заменяем serviceRouting на configureRouting

    install(StatusPages) {
        exception<IllegalArgumentException> { call, cause ->
            call.respondError(HttpStatusCode.BadRequest, cause.message ?: "Invalid argument")
        }

        exception<com.fasterxml.jackson.databind.exc.MismatchedInputException> { call, cause ->
            call.respondError(HttpStatusCode.BadRequest, cause.message ?: "Mismatched input")
        }

        exception<Throwable> { call, cause ->
            LoggerFactory.getLogger("InternalError").error("Unhandled exception", cause)
            call.respondError(HttpStatusCode.InternalServerError, "Internal server error")
        }
    }
}

data class ErrorResponse(val error: String)
