package mobi.sevenwinds.app.author

import io.restassured.RestAssured
import io.restassured.parsing.Parser
import mobi.sevenwinds.common.ServerTest
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class AuthorApiKtTest : ServerTest() {

    companion object {
        @JvmStatic
        @BeforeAll
        fun setupRestAssured() {
            RestAssured.defaultParser = Parser.JSON
        }
    }

    @BeforeEach
    internal fun setUp() {
        transaction { AuthorTable.deleteAll() }
    }

    @Test
    fun testCreateAuthor() {
        val fullName = "John Doe"
        val authorId = RestAssured.given()
            .contentType("application/json")
            .body(mapOf("fullName" to fullName))
            .post("/author/add")
            .then()
            .statusCode(200)
            .extract()
            .path<Int>("id")

        transaction {
            val author = Author.findById(authorId)
            assertNotNull(author)
            assertEquals(fullName, author?.fullName)
            assertNotNull(author?.createdAt)
        }
    }
}
