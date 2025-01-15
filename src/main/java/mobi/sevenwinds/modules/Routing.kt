package mobi.sevenwinds.modules

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.http.content.*
import io.ktor.server.request.*
import mobi.sevenwinds.app.budget.BudgetRecord
import mobi.sevenwinds.app.budget.BudgetService

/**
 * Конфигурация маршрутов приложения
 */
fun Application.configureRouting() {
    routing {
        get("/") {
            call.respondRedirect("/swagger-ui/index.html?url=/openapi.json", true)
        }

        get("/openapi.json") {
            call.respond(generateOpenApiSpec()) // Генерация OpenAPI спецификации
        }

        static("/swagger-ui") {
            resources("swagger-ui")
        }

        post("/budget/add") {
            val record = call.receive<BudgetRecord>()
            println("Received record: $record")
            val savedRecord = BudgetService.addRecord(record)
            println("Saved record: $savedRecord")
            call.respond(savedRecord)
        }
    }
}
