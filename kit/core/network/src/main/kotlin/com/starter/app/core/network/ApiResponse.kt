package com.starter.app.core.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ApiResponse<out T>(
    @SerialName("data")
    val data: T?,
    @SerialName("error")
    val message: String?,
)