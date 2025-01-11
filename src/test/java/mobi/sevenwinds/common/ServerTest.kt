package mobi.sevenwinds.common

import com.typesafe.config.ConfigFactory
import io.ktor.server.config.HoconApplicationConfig
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.restassured.RestAssured
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.jetbrains.exposed.sql.Database
import org.junit.jupiter.api.BeforeAll
import java.util.concurrent.TimeUnit

open class ServerTest {
    companion object {
        private var serverStarted = false

        @JvmStatic
        fun initDatabase() {
            Database.connect(
                url = "jdbc:postgresql://localhost:45533/dev_mem",
                driver = "org.postgresql.Driver",
                user = "dev",
                password = "dev"
            )
        }

        // Изменяем тип на EmbeddedServer
        private lateinit var server: EmbeddedServer<NettyApplicationEngine, NettyApplicationEngine.Configuration>

        @ExperimentalCoroutinesApi
        @BeforeAll
        @JvmStatic
        fun startServer() {
            if (!serverStarted) {
                initDatabase()

                val hoconConfig = HoconApplicationConfig(ConfigFactory.load("test.conf"))
                val port = hoconConfig.property("ktor.deployment.port").getString().toInt()

                // Запуск embeddedServer
                server = embeddedServer(Netty, port = port) {
                    // Здесь вы можете настроить модули и роуты, если необходимо
                }.start(wait = false)

                serverStarted = true

                // Настройка RestAssured
                RestAssured.baseURI = "http://localhost"
                RestAssured.port = port
                RestAssured.enableLoggingOfRequestAndResponseIfValidationFails()

                // Добавляем shutdown hook
                Runtime.getRuntime().addShutdownHook(Thread {
                    server.stop(0, 0, TimeUnit.SECONDS)
                })
            }
        }
    }
}
