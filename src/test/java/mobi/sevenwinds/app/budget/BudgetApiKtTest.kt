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
        addRecord(BudgetRecord(2020, 5, 10, BudgetType.Приход))
        addRecord(BudgetRecord(2020, 5, 5, BudgetType.Приход))
        addRecord(BudgetRecord(2020, 5, 20, BudgetType.Приход))
        addRecord(BudgetRecord(2020, 5, 30, BudgetType.Приход))
        addRecord(BudgetRecord(2020, 5, 40, BudgetType.Приход))
        addRecord(BudgetRecord(2030, 1, 1, BudgetType.Расход))

        // Запрос с параметрами пагинации
        RestAssured.given()
            .queryParam("limit", 3)
            .queryParam("offset", 1)
            .get("/budget/year/2020/stats")
            .toResponse<BudgetYearStatsResponse>().let { response ->
                println("Total: ${response.total}")
                println("Items: ${response.items}")
                println("TotalByType: ${response.totalByType}")

                assertEquals(5, response.total)

                assertEquals(3, response.items.size)

                assertEquals(105, response.totalByType[BudgetType.Приход.name])
            }
    }

    @Test
    fun testStatsSortOrder() {
        addRecord(BudgetRecord(2020, 5, 100, BudgetType.Приход))
        addRecord(BudgetRecord(2020, 1, 5, BudgetType.Приход))
        addRecord(BudgetRecord(2020, 5, 50, BudgetType.Приход))
        addRecord(BudgetRecord(2020, 1, 30, BudgetType.Приход))
        addRecord(BudgetRecord(2020, 5, 400, BudgetType.Приход))

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

    private fun addRecord(record: BudgetRecord) {
        try {
            val response = RestAssured.given()
                .contentType("application/json")
                .body(record)
                .post("/budget/add")

            response.then().statusCode(200)

            // Получаем тело ответа
            val responseBody = response.asString()
            if (responseBody.isNullOrBlank()) {
                fail("Server returned empty or null response for record: $record")
            } else {
                val responseObject = response.toResponse<BudgetRecord>()
                assertEquals(record, responseObject, "Saved and retrieved records do not match!")
            }
        } catch (ex: Exception) {
            fail("Error occurred while adding record: ${ex.message}")
        }
    }
}