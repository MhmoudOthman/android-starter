package com.starter.app.core.network

import android.content.Context
import android.os.Environment
import android.util.Log
import com.starter.app.core.common.exceptions.AppCodes
import com.starter.app.core.common.exceptions.AppException
import com.starter.app.core.model.Result
import io.ktor.client.call.body
import io.ktor.client.plugins.ResponseException
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.contentLength
import io.ktor.http.isSuccess
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.readAvailable
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import java.io.File
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Maps a transport-level failure onto a [Result.Error] with a code a caller can branch on.
 * Cancellation is rethrown, never mapped — a cancelled coroutine is not a failed request.
 */
fun Throwable.toNetworkError(): Result.Error = when (this) {
    is CancellationException -> throw this
    is AppException -> Result.Error(this, message)
    is ResponseException -> Result.Error(
        AppException(response.status.description, response.status.value),
        response.status.description,
    )
    is UnknownHostException -> Result.Error(
        AppException("No internet connection", AppCodes.SERVICE_UNAVAILABLE),
        "No internet connection",
    )
    is SocketTimeoutException -> Result.Error(
        AppException(message, AppCodes.REQUEST_TIMEOUT),
        message,
    )
    is ConnectException -> Result.Error(
        AppException(message ?: "Unable to connect", AppCodes.SERVICE_UNAVAILABLE),
        message ?: "Unable to connect",
    )
    is SerializationException -> Result.Error(
        AppException("Failed to parse server response", AppCodes.UNPROCESSABLE_ENTITY),
        "Failed to parse server response",
    )
    is IOException -> Result.Error(
        AppException(message, AppCodes.SERVICE_UNAVAILABLE),
        message,
    )
    else -> Result.Error(AppException(message, AppCodes.UNDEFINED), message)
}

suspend fun <T> FlowCollector<Result<T>>.emitNetworkError(e: Throwable) = emit(e.toNetworkError())

/**
 * The only thing this app logs about a network call: **a status code and how long it took**.
 *
 * Nothing else is safe to print, and the rule is deliberately absolute rather than tuned per call
 * site: request bodies carry passwords, response bodies carry tokens, the `Authorization` header is
 * on every call, and a path parameter can leak in the URL itself — which is why Ktor's `Logging`
 * plugin is not installed at all, at any level. If a problem ever needs the endpoint named, add an
 * explicit `label` parameter and pass a constant: a written-down decision, not a derived string.
 */
@PublishedApi
internal fun logApiCall(statusCode: Int, elapsedMs: Long) {
    if (BuildConfig.DEBUG) Log.d(API_TAG, "api call -> $statusCode in ${elapsedMs}ms")
}

@PublishedApi
internal const val API_TAG: String = "ApiCall"

/** Reads a successful envelope, treating a null `data` as an error when a body was expected. */
@PublishedApi
internal suspend inline fun <reified T> HttpResponse.toResult(emptyDataMessage: String): Result<T> {
    if (!status.isSuccess()) {
        // Parsed as ApiError, NEVER as ApiResponse<T>: failure bodies have a different shape, and
        // reading one through the success envelope throws — erasing the status code the caller
        // branches on.
        val message = errorMessage()
        return Result.Error(AppException(message, status.value), message)
    }
    val envelope = body<ApiResponse<T>>()
    val data = envelope.data
    return if (data != null) {
        Result.Success(data)
    } else {
        val message = envelope.message ?: emptyDataMessage
        Result.Error(AppException(message, status.value), message)
    }
}

/**
 * A one-shot request as a plain [Result] — the default for every API method.
 *
 * There is deliberately no `Loading` here: the caller (a ViewModel) sets its own `isLoading` around
 * the call, which keeps every collector to two cases and keeps "when to show the overlay" a UI
 * decision. A `Flow` carrying [Result.Loading] is reserved for genuine streams — [saveFile]'s
 * progress, or a Room observation.
 */
suspend inline fun <reified T> apiCall(
    emptyDataMessage: String,
    crossinline call: suspend () -> HttpResponse,
): Result<T> = withContext(Dispatchers.IO) {
    try {
        val startedAt = System.currentTimeMillis()
        val response = call()
        logApiCall(response.status.value, System.currentTimeMillis() - startedAt)
        response.toResult<T>(emptyDataMessage)
    } catch (e: Exception) {
        e.toNetworkError()
    }
}

/**
 * A one-shot request whose success carries no payload (a write answering `204`, or a null `data`).
 * [apiCall] reads a null `data` as an error, which is right when a body was expected and wrong
 * here, so the two are kept apart rather than folded together behind a flag.
 */
suspend inline fun apiCallNoContent(
    crossinline call: suspend () -> HttpResponse,
): Result<Unit> = withContext(Dispatchers.IO) {
    try {
        val startedAt = System.currentTimeMillis()
        val response = call()
        logApiCall(response.status.value, System.currentTimeMillis() - startedAt)
        if (response.status.isSuccess()) {
            Result.Success(Unit)
        } else {
            val message = response.errorMessage()
            Result.Error(AppException(message, response.status.value), message)
        }
    } catch (e: Exception) {
        e.toNetworkError()
    }
}

/**
 * The streaming form: emits [Result.Loading] first, then the outcome. Use it only where a caller
 * genuinely consumes a stream (e.g. combining a request with a Room flow); for a plain request
 * prefer [apiCall].
 */
inline fun <reified T> apiCallFlow(
    emptyDataMessage: String,
    crossinline call: suspend () -> HttpResponse,
): Flow<Result<T>> = flow {
    emit(Result.Loading())
    emit(apiCall<T>(emptyDataMessage, call))
}

/**
 * Streams a response body to a file under the app's external downloads directory, emitting the
 * percentage as [Result.Loading] per chunk — the one place progress is genuinely a stream.
 */
fun HttpResponse.saveFile(context: Context, fileName: String): Flow<Result<File>> = flow {
    emit(Result.Loading(0))
    val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), fileName)
    val totalBytes = contentLength() ?: -1L
    val channel: ByteReadChannel = bodyAsChannel()
    try {
        file.outputStream().use { output ->
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            var progressBytes = 0L

            while (!channel.isClosedForRead) {
                val bytesRead = channel.readAvailable(buffer)
                if (bytesRead > 0) {
                    output.write(buffer, 0, bytesRead)
                    progressBytes += bytesRead
                    if (totalBytes > 0) {
                        val progress = ((progressBytes * 100) / totalBytes).toInt()
                        emit(Result.Loading(progress))
                    }
                }
            }
        }
        emit(Result.Success(file))
    } catch (e: Exception) {
        emitNetworkError(e)
    }
}.flowOn(Dispatchers.IO).distinctUntilChanged()
