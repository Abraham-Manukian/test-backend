package mobi.sevenwinds.modules

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.servers.Server
import io.swagger.v3.oas.models.responses.ApiResponse
import io.swagger.v3.oas.models.media.Content
import io.swagger.v3.oas.models.media.MediaType
import io.swagger.v3.oas.models.media.Schema
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.response.*
import io.swagger.v3.oas.models.PathItem
import io.swagger.v3.oas.models.Paths
import io.swagger.v3.oas.models.responses.ApiResponses
import mobi.sevenwinds.Const

/**
 * Инициализация Swagger/OpenAPI
 */
fun Application.initSwagger() {
    val openApi = generateOpenApiSpec()

    routing {
        // Маршрут для получения OpenAPI спецификации
        get("/openapi.json") {
            call.respond(openApi)
        }

        // Swagger UI интерфейс
        get("/") {
            call.respondRedirect("/swagger-ui/index.html?url=/openapi.json", true)
        }
    }
}

/**
 * Генерация OpenAPI спецификации
 */
fun generateOpenApiSpec(): OpenAPI {
    val openApi = OpenAPI().apply {
        info = Info().apply {
            title = "Construction Dashboard"
            version = Const.version
            description = "Backend API"
        }
        servers = listOf(
            Server().url("http://localhost:8080/").description("Local server")
        )
        paths = Paths().apply {
            addPathItem("/example", createExamplePath())
            addPathItem("/budget", createBudgetPath()) // Добавить дополнительные маршруты
        }
    }

    println("Swagger доступен по адресу: http://localhost:8080/swagger-ui/index.html?url=/openapi.json")
    return openApi
}

/**
 * Создание примера маршрута `/example`
 */
fun createExamplePath(): PathItem {
    return PathItem().apply {
        get = io.swagger.v3.oas.models.Operation().apply {
            summary = "Пример запроса"
            responses = ApiResponses().apply {
                addApiResponse("200", ApiResponse().apply {
                    description = "Успешный ответ"
                    content = Content().apply {
                        addMediaType("application/json", MediaType().apply {
                            schema = Schema<Map<String, String>>().apply {
                                type = "object"
                                addProperty("message", Schema<String>().apply { type = "string" })
                            }
                        })
                    }
                })
            }
        }
    }
}

/**
 * Создание маршрута `/budget`
 */
fun createBudgetPath(): PathItem {
    return PathItem().apply {
        get = io.swagger.v3.oas.models.Operation().apply {
            summary = "Получить бюджет"
            responses = ApiResponses().apply {
                addApiResponse("200", ApiResponse().apply {
                    description = "Список бюджетов"
                    content = Content().apply {
                        addMediaType("application/json", MediaType().apply {
                            schema = Schema<List<BudgetResponse>>().apply {
                                type = "array"
                                items = Schema<BudgetResponse>().apply {
                                    addProperty("id", Schema<Int>().apply { type = "integer" })
                                    addProperty("amount", Schema<Double>().apply { type = "number" })
                                    addProperty("description", Schema<String>().apply { type = "string" })
                                }
                            }
                        })
                    }
                })
            }
        }
    }
}

/**
 * Модель ответа для `/budget`
 */
data class BudgetResponse(
    val id: Int,
    val amount: Double,
    val description: String
)
