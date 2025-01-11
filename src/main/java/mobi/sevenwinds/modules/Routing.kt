package mobi.sevenwinds.modules

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.http.content.*

/**
 * Конфигурация маршрутов приложения
 */
fun Application.configureRouting() {
    routing {
        // Перенаправление на Swagger UI
        get("/") {
            call.respondRedirect("/swagger-ui/index.html?url=/openapi.json", true)
        }

        // Маршрут для OpenAPI спецификации
        get("/openapi.json") {
            call.respond(generateOpenApiSpec()) // Генерация OpenAPI спецификации
        }

        // Статические ресурсы для Swagger UI
        static("/swagger-ui") {
            resources("swagger-ui")
        }
    }
}
