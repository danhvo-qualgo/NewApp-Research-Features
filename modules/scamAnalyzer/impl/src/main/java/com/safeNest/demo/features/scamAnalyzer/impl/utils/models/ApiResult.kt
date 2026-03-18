package com.safeNest.demo.features.scamAnalyzer.impl.utils.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

sealed class ApiResult<out T : Any> {
    data class Success<out T : Any>(
        val data: T,
        val rawResponse: JsonObject? = null,
    ) : ApiResult<T>()

    data class Error(
        val error: ErrorResponse,
    ) : ApiResult<Nothing>()

    data class Exception(
        val throwable: Throwable,
    ) : ApiResult<Nothing>()
}

@Serializable
data class ErrorResponse(
    val statusCode: Int,
    val title: String,
    val detail: String,
    val errorCode: String,
    val errorParams: JsonObject,
)
