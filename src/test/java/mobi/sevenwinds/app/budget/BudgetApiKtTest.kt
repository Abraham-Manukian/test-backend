package mobi.sevenwinds.app.budget

import io.restassured.RestAssured
import mobi.sevenwinds.common.ServerTest
import mobi.sevenwinds.common.jsonBody
import mobi.sevenwinds.common.toResponse
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import io.restassured.parsing.Parser
import mobi.sevenwinds.app.budget.BudgetService.addBudgetRecord
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.select

class BudgetApiKtTest : ServerTest() {

    companion object {
        @JvmStatic
        @BeforeAll
        fun setupRestAssured() {
            RestAssured.defaultParser = Parser.JSON
        }
    }

    @BeforeEach
    internal fun setUp() {
        transaction { BudgetTable.deleteAll() }
    }

    @Test
    fun testBudgetPagination() {
        addBudgetRecord(BudgetRecord(2020, 5, 10, BudgetType.Приход))
        addBudgetRecord(BudgetRecord(2020, 5, 5, BudgetType.Приход))
        addBudgetRecord(BudgetRecord(2020, 5, 20, BudgetType.Приход))
        addBudgetRecord(BudgetRecord(2020, 5, 30, BudgetType.Приход))
        addBudgetRecord(BudgetRecord(2020, 5, 40, BudgetType.Приход))

        RestAssured.given()
            .queryParam("limit", 3)
            .queryParam("offset", 1)
            .get("/budget/year/2020/stats")
            .toResponse<BudgetYearStatsResponse>().let { response ->
                assertEquals(5, response.total)
                assertEquals(3, response.items.size)
                assertEquals(105, response.totalByType[BudgetType.Приход])
            }
    }

    @Test
    fun testStatsSortOrder() {
        addBudgetRecord(BudgetRecord(2020, 5, 100, BudgetType.Приход))
        addBudgetRecord(BudgetRecord(2020, 1, 5, BudgetType.Приход))
        addBudgetRecord(BudgetRecord(2020, 5, 50, BudgetType.Приход))
        addBudgetRecord(BudgetRecord(2020, 1, 30, BudgetType.Приход))
        addBudgetRecord(BudgetRecord(2020, 5, 400, BudgetType.Приход))
        // expected sort order - month ascending, amount descending

        RestAssured.given()
            .get("/budget/year/2020/stats?limit=100&offset=0")
            .toResponse<BudgetYearStatsResponse>().let { response ->
                println(response.items)

                assertEquals(30, response.items[0].amount)
                assertEquals(5, response.items[1].amount)
                assertEquals(400, response.items[2].amount)
                assertEquals(100, response.items[3].amount)
                assertEquals(50, response.items[4].amount)
            }
    }

    @Test
    fun testInvalidMonthValues() {
        RestAssured.given()
            .jsonBody(BudgetRecord(2020, -5, 5, BudgetType.Приход))
            .post("/budget/add")
            .then().statusCode(400)

        RestAssured.given()
            .jsonBody(BudgetRecord(2020, 15, 5, BudgetType.Приход))
            .post("/budget/add")
            .then().statusCode(400)
    }
}