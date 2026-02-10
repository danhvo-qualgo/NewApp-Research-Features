package net.qualgo.safeNest.core.authChallenge.impl

import kotlinx.serialization.json.JsonObject

sealed class ApiResult<out T> {
    data class Success<T>(val data: T, val rawResponse: JsonObject) : ApiResult<T>()
    data class Error(val errorMessage: String) : ApiResult<Nothing>()
    data class Exception(val exception: Throwable) : ApiResult<Nothing>()
}