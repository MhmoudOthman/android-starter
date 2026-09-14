package com.starter.app.core.network

import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A failed response's body, read for its message and nothing else.
 *
 * Deliberately *not* [ApiResponse]: on an error the backend fills `data` with a different shape from
 * the one the call asked for — `GET items/{code}` might answer `{"data": 3}` on a hit and
 * `{"data": false, "error": "Order Not Found"}` on a miss — so parsing a failure through the success
 * envelope cannot work, and the exception it raises is worse than useless: it is swallowed by the
 * surrounding `catch` and re-emitted as [AppCodes.UNDEFINED], erasing the very status code the
 * caller branches on.
 */
@Serializable
data class ApiError(
    @SerialName("error") val message: String? = null,
)

/**
 * The backend's own words for why a call failed, falling back to the status line.
 *
 * The fallback is what keeps the status code meaningful: a body that will not parse at all — an HTML
 * page from a proxy, an empty response — must still leave the caller with a real `404` or `403`
 * rather than an undefined error.
 */
suspend fun HttpResponse.errorMessage(): String =
    try {
        body<ApiError>().message?.takeIf { it.isNotBlank() }
    } catch (e: CancellationException) {
        throw e
    } catch (_: Exception) {
        null
    } ?: status.description
