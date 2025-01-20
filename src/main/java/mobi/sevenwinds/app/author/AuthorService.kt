package mobi.sevenwinds.app.author

import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime


object AuthorService {
    fun addAuthor(fullName: String): Author {
        return transaction {
            Author.new {
                this.fullName = fullName
                this.createdAt = DateTime.now()
            }
        }
    }
}
