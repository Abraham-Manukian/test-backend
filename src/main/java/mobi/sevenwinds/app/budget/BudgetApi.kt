package mobi.sevenwinds.app.budget

import io.ktor.server.routing.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.request.*

fun Application.configureBudgetRoutes() {
    routing {
        route("/budget") {
            post("/add") {
                val budgetRecord = call.receive<BudgetRecord>()
                val addedRecord = BudgetService.addRecord(budgetRecord)
                call.respond(addedRecord)
            }

            get("/year/{year}/stats") {
                val year = call.parameters["year"]?.toIntOrNull()
                    ?: return@get call.respondText("Year is missing or invalid", status = io.ktor.http.HttpStatusCode.BadRequest)

                val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 10
                val offset = call.request.queryParameters["offset"]?.toIntOrNull() ?: 0

                val statsResponse = BudgetService.getYearStats(BudgetYearParam(year, limit, offset))
                call.respond(statsResponse)
            }
        }
    }
}

data class BudgetRecord(
    val year: Int,
    val month: Int,
    val amount: Int,
    val type: BudgetType
)

data class BudgetYearParam(
    val year: Int,
    val limit: Int = 10,
    val offset: Int = 0,
)

data class BudgetYearStatsResponse(
    val total: Int,
    val totalByType: Map<String, Int>,
    val items: List<BudgetRecord>
)

enum class BudgetType {
    Приход, Расход, Комиссия
}
