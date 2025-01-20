package mobi.sevenwinds.app.author

import kotlinx.serialization.Serializable
import org.joda.time.DateTime

@Serializable
data class AuthorRecord(
    val fullName: String,
)


data class AuthorResponse(
    val fullName: String?,
    val createdAt: DateTime?
)