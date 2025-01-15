package mobi.sevenwinds.common

import com.typesafe.config.ConfigFactory
import io.ktor.server.config.HoconApplicationConfig
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.restassured.RestAssured
import io.restassured.parsing.Parser
import kotlinx.coroutines.ExperimentalCoroutinesApi
import mobi.sevenwinds.app.budget.configureBudgetRoutes
import mobi.sevenwinds.configureSerialization
import mobi.sevenwinds.modules.configureRouting
import org.jetbrains.exposed.sql.Database
import org.junit.jupiter.api.BeforeAll
import java.util.concurrent.TimeUnit

open class ServerTest {
    companion object {
        private var serverStarted = false

        @JvmStatic
        fun initDatabase() {
            Database.connect(
                url = "jdbc:postgresql://localhost:5432/dev_mem",
                driver = "org.postgresql.Driver",
                user = "dev",
                password = "dev"
            )
        }

        private lateinit var server: EmbeddedServer<NettyApplicationEngine, NettyApplicationEngine.Configuration>

        @ExperimentalCoroutinesApi
        @BeforeAll
        @JvmStatic
        fun startServer() {
            if (!serverStarted) {
                initDatabase()

                val config = HoconApplicationConfig(ConfigFactory.load("test.conf"))
                val port = config.propertyOrNull("ktor.deployment.port")?.getString()?.toIntOrNull() ?: 0

                server = embeddedServer(Netty, port = port) {
                    configureRouting()
                    configureBudgetRoutes()
                    configureSerialization()
                }.start(wait = false)

                val serverPort = server.environment.config.propertyOrNull("ktor.deployment.port")?.getString()?.toIntOrNull() ?: port
                    .takeIf { it > 0 } ?: throw IllegalStateException("Не удалось определить порт сервера.")

                RestAssured.port = serverPort
                println("Тестовый сервер запущен на порту: $serverPort")
                serverStarted = true

                RestAssured.baseURI = "http://localhost"
                RestAssured.port = port
                RestAssured.enableLoggingOfRequestAndResponseIfValidationFails()

                Runtime.getRuntime().addShutdownHook(Thread {
                    server.stop(0, 0, TimeUnit.SECONDS)
                })
            }
        }
    }
}
