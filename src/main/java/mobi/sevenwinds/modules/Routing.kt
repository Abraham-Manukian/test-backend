package mobi.sevenwinds.modules

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.http.content.*
import io.ktor.server.request.*
import mobi.sevenwinds.app.author.AuthorRecord
import mobi.sevenwinds.app.author.AuthorService
import mobi.sevenwinds.app.author.AuthorTable
import mobi.sevenwinds.app.budget.BudgetRecord
import mobi.sevenwinds.app.budget.BudgetService
import mobi.sevenwinds.app.budget.BudgetTable
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime
import java.time.LocalDateTime

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

        post("/author/add") {
            val authorRequest = call.receive<AuthorRecord>()
            val addedAuthor = AuthorService.addAuthor(authorRequest.fullName)
            call.respond(mapOf("id" to addedAuthor.id.value))
        }
    }
}
