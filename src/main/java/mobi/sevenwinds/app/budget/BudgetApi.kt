package mobi.sevenwinds.app.budget

import io.ktor.http.*
import io.ktor.server.routing.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import kotlinx.serialization.Serializable
import mobi.sevenwinds.app.author.Author
import mobi.sevenwinds.app.author.AuthorResponse
import mobi.sevenwinds.app.author.AuthorTable
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime
import java.time.ZoneId

fun Application.configureBudgetRoutes() {
    routing {
        route("/budget") {
            post("/add") {
                val budgetRecord = call.receive<BudgetRecord>()
                val addedRecord = BudgetService.addBudgetRecord(budgetRecord)

                // Проверка корректности месяца
                if (budgetRecord.month < 1 || budgetRecord.month > 12) {
                    call.respond(HttpStatusCode.BadRequest, "Invalid month value")
                    return@post
                }
                call.respond(HttpStatusCode.Created, addedRecord)
            }

            get("/year/{year}/stats") {
                val year = call.parameters["year"]?.toIntOrNull()
                if (year == null) {
                    return@get call.respond(HttpStatusCode.BadRequest, "Year is required")
                }

                val authorNameFilter = call.request.queryParameters["authorName"]
                val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 10
                val offset = call.request.queryParameters["offset"]?.toIntOrNull() ?: 0

                val stats = BudgetService.getBudgetStats(year, authorNameFilter, limit, offset)

                call.respond(stats)
            }
        }
    }
}

@Serializable
data class BudgetRecord(
    val year: Int,
    val month: Int,
    val amount: Int,
    val type: BudgetType,
    val authorId: Int? = null,
    val author: AuthorResponse? = null
)

data class BudgetYearParam(
    val year: Int,
    val limit: Int = 10,
    val offset: Int = 0,
)

@Serializable
data class BudgetYearStatsResponse(
    val total: Int,
    val totalByType: Map<BudgetType, Int>,
    val items: List<BudgetRecord>
)


enum class BudgetType {
    Приход, Расход, Комиссия
}
