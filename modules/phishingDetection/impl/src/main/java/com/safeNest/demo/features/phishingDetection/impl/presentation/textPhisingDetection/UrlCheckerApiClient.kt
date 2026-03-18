package net.qualgo.safeNest.features.phishingDetection.impl.presentation.textPhisingDetection

import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import net.qualgo.safeNest.features.phishingDetection.impl.presentation.models.ApiResult
import net.qualgo.safeNest.features.phishingDetection.impl.presentation.models.ErrorResponse
import net.qualgo.safeNest.features.phishingDetection.impl.presentation.models.UrlCheckerResponse

@Serializable
private data class UrlAnalyzeRequest(val url: String)

object UrlCheckerApiClient {

    private const val TAG = "UrlCheckerApiClient"
    private const val ENDPOINT = "http://192.168.122.230:8000/api/v1.0/urls/analyze" //update later

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private val httpClient: HttpClient by lazy {
        HttpClient(Android) {
            install(ContentNegotiation) {
                json(json)
            }
            install(Logging) {
                level = LogLevel.BODY
                logger = object : Logger {
                    override fun log(message: String) {
                        Log.d(TAG, message)
                    }
                }
            }
            engine {
                connectTimeout = 15_000
                socketTimeout = 30_000
            }
        }
    }

    suspend fun analyze(url: String): ApiResult<UrlCheckerResponse> {
        return try {
            val response = httpClient.post(ENDPOINT) {
                contentType(ContentType.Application.Json)
                header("x-language", "en")
                header("x-correlation-id", "6d9bb21c-f7f3-4a92-b90f-d8e00ee78ba0") //Mock value
                header("x-device-id", "4a929bb0fd8e00b90f7f") //Mock value
                setBody(UrlAnalyzeRequest(url = url))
            }

            val bodyText = response.bodyAsText()

            if (response.status.isSuccess()) {
                val root = json.parseToJsonElement(bodyText).jsonObject
                val dataElement = root["data"]?.jsonObject ?: root
                val data = json.decodeFromJsonElement(UrlCheckerResponse.serializer(), dataElement)
                ApiResult.Success(data = data, rawResponse = root)
            } else {
                val error = json.decodeFromString(ErrorResponse.serializer(), bodyText)
                ApiResult.Error(error)
            }
        } catch (e: Exception) {
            Log.e(TAG, "analyze failed for $url: ${e.message}", e)
            ApiResult.Exception(e)
        }
    }

    fun close() {
        httpClient.close()
    }
}
