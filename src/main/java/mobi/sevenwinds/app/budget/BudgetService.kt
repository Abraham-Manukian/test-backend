package mobi.sevenwinds.app.budget

import mobi.sevenwinds.app.author.AuthorResponse
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import mobi.sevenwinds.app.author.AuthorTable
import org.jetbrains.exposed.dao.EntityID

object BudgetService {
    fun addBudgetRecord(budgetRecord: BudgetRecord): BudgetRecord {

        return transaction {
            val addedRecord = BudgetTable.insertAndGetId {
                it[year] = budgetRecord.year
                it[month] = budgetRecord.month
                it[amount] = budgetRecord.amount
                it[type] = budgetRecord.type
                if (budgetRecord.authorId != null) {
                    it[authorId] = EntityID(budgetRecord.authorId, AuthorTable)
                }
            }
            budgetRecord.copy()
        }
    }

    fun getBudgetStats(year: Int, authorNameFilter: String?, limit: Int, offset: Int): BudgetYearStatsResponse {
        return transaction {
            val baseQuery = BudgetTable
                .join(AuthorTable, JoinType.LEFT, BudgetTable.authorId, AuthorTable.id)
                .select { BudgetTable.year eq year }
                .let {
                    if (!authorNameFilter.isNullOrBlank()) {
                        it.andWhere { AuthorTable.fullName.lowerCase() like "%${authorNameFilter.lowercase()}%" }
                    } else it
                }

            val total = baseQuery.count()

            if (total == 0) {
                println("No records found for year: $year with author filter: $authorNameFilter")
            }

            val sortedQuery = baseQuery
                .orderBy(BudgetTable.month to SortOrder.ASC, BudgetTable.amount to SortOrder.DESC)

            val totalByType = sortedQuery
                .groupBy { it[BudgetTable.type] }
                .mapValues { (_, group) -> group.sumOf { it[BudgetTable.amount] } }

            val items = sortedQuery
                .limit(limit, offset)
                .map {
                    BudgetRecord(
                        year = it[BudgetTable.year],
                        month = it[BudgetTable.month],
                        amount = it[BudgetTable.amount],
                        type = it[BudgetTable.type],
                        author = AuthorResponse(
                            fullName = it[AuthorTable.fullName],
                            createdAt = it[AuthorTable.createdAt]
                        )
                    )
                }

            BudgetYearStatsResponse(
                total = total,
                totalByType = totalByType,
                items = items
            )
        }
    }
}
