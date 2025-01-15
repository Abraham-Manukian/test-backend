package mobi.sevenwinds.modules

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.config.ApplicationConfig
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database

object DatabaseFactory {

    lateinit var appConfig: ApplicationConfig

    private val dbDriver: String by lazy { appConfig.property("db.jdbcDriver").getString() }
    private val dbUrl: String by lazy { appConfig.property("db.jdbcUrl").getString() }
    private val dbUser: String by lazy { appConfig.property("db.dbUser").getString() }
    private val dbPassword: String by lazy { appConfig.property("db.dbPassword").getString() }

    fun init(config: ApplicationConfig) {
        appConfig = config

        Database.connect(hikari())

        val flyway = Flyway.configure().dataSource(dbUrl, dbUser, dbPassword)
            .locations("classpath:db/migration")
            .outOfOrder(true)
            .load()

        if (appConfig.propertyOrNull("flyway.clean")?.getString()?.toBoolean() == true) {
            flyway.clean()
        }

        flyway.migrate()
    }

    private fun hikari(): HikariDataSource {
        val config = HikariConfig().apply {
            driverClassName = dbDriver
            jdbcUrl = dbUrl
            username = dbUser
            password = dbPassword
            maximumPoolSize = appConfig.propertyOrNull("db.maxPoolSize")?.getString()?.toInt() ?: 10
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
        }
        config.validate()
        return HikariDataSource(config)
    }
}
