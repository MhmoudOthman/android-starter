package com.starter.app.core.network

import com.starter.app.core.common.exceptions.AppException
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.HttpTimeoutConfig
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.accept
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

object HttpClientFactory {
    fun create(): HttpClient = HttpClient(OkHttp) {
        // No `Logging` plugin, at any level, in any build type.
        //
        // It used to be installed at `LogLevel.ALL` under `BuildConfig.DEBUG`, on the reasoning
        // that a release build logs nothing. That reasoning was too narrow twice over. A debug
        // build runs on real handsets against staging with real credentials, and `ALL` prints the
        // request body (the driver's password on `login`, their live coordinates on
        // `change-status-v2`), the response body (the auth token), and the `Authorization` header.
        //
        // And there is no lower level that is safe either: `scan-orders/{barcode}` puts the
        // barcode in the *path*, so even `LogLevel.INFO` — method and URL only — leaks it.
        // Redacting the header while keeping bodies would have missed the point entirely.
        //
        // What is left is `logApiCall` in `Ext.kt`: a status code and a duration, nothing more.
        install(HttpTimeout) {
            requestTimeoutMillis = HttpTimeoutConfig.INFINITE_TIMEOUT_MS // مهم جداً للـ download
            socketTimeoutMillis = 60_000

        }
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                ignoreUnknownKeys = true
                isLenient = true
                explicitNulls = false
            })
        }
        HttpResponseValidator {
            validateResponse { response ->
                val statusCode = response.status.value
                if (statusCode >= 500) {
                    val body = response.body<ApiResponse<Unit>>()
                    throw AppException(
                        body.message ?: response.status.description,
                        response.status.value
                    )
                }
            }

            handleResponseExceptionWithRequest { exception, request ->
                val clientException = exception as? ClientRequestException
                    ?: return@handleResponseExceptionWithRequest

                val response = clientException.response
                // Handle non-JSON responses
                if (response.contentType()?.contentType != "application") {

                    throw AppException(
                        response.bodyAsText().ifBlank {
                            response.status.description
                        }, response.status.value
                    )
                }
            }
        }

        defaultRequest {
            contentType(ContentType.Application.Json)
            accept(ContentType.Application.Json)
            url(BuildConfig.BASE_URL)
        }
    }
}
