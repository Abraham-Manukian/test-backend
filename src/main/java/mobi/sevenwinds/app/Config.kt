package mobi.sevenwinds.app

import io.ktor.server.application.*

class Config(application: Application) {
    private val config = application.environment.config

    val logAllRequests: Boolean
        get() = config.propertyOrNull("ktor.logAllRequests")?.getString()?.toBoolean() ?: false

    val databaseUrl: String
        get() = config.propertyOrNull("ktor.database.url")?.getString() ?: "jdbc:postgresql://localhost:5432/mydb"

    val databaseUser: String
        get() = config.propertyOrNull("ktor.database.user")?.getString() ?: "user"

    val databasePassword: String
        get() = config.propertyOrNull("ktor.database.password")?.getString() ?: "password"
}
