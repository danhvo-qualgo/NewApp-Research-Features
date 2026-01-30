package com.safeNest.features.core.authChallenge.impl.data.source

import com.safeNest.features.core.authChallenge.impl.domain.model.AuthChallengeFlow
import com.safeNest.features.core.authChallenge.impl.domain.model.AuthChallengeName
import com.safeNest.features.core.authChallenge.impl.domain.model.ChallengeParametersKey
import com.safeNest.features.core.authChallenge.impl.domain.model.ClientMetadataKey
import com.safeNest.features.core.authChallenge.impl.domain.model.ClientMetadataValue
import com.safeNest.features.core.authChallenge.impl.ApiResult
import com.safeNest.features.core.authChallenge.impl.JSON
import kotlinx.coroutines.delay
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObjectBuilder
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import java.util.UUID
import kotlin.random.Random

internal object MockRemoteSource {

    suspend fun processAuthChallenge(request: AuthChallengeRequest): ApiResult<AuthChallengeResponse> {
        println("[Request] $request")

        delay(500) // simulate network request

        return when (request.authFlow) {
            AuthChallengeFlow.LOGIN -> getSignInResponse(request)

            else -> ApiResult.Exception(
                IllegalArgumentException("Invalid authFlow: ${request.authFlow}")
            )
        }.also {
            println("[Response] $it")
        }
    }

    private fun getSignInResponse(
        request: AuthChallengeRequest
    ): ApiResult<AuthChallengeResponse> {
        return if (AuthChallengeName.INIT_CHALLENGE == request.challengeName) {
            getInitChallengeResponse(request)
        } else {
            getVerifyResponse(request)
        }
    }

    private fun getInitChallengeResponse(
        request: AuthChallengeRequest
    ): ApiResult<AuthChallengeResponse> {
        val signInType = request.clientMetadata?.get(ClientMetadataKey.TYPE)
            ?.jsonPrimitive?.content
        return when (signInType) {
            ClientMetadataValue.SIGN_IN_TYPE_EMAIL -> {
                val response = AuthChallengeResponse(
                    authFlow = request.authFlow,
                    challengeName = AuthChallengeName.VERIFY_OTP,
                    session = UUID.randomUUID().toString(),
                    challengeParameters = buildJsonObject {
                        putTimestamp()
                        putExpiryTime()
                    }
                )
                val rawResponse = JSON.encodeToJsonElement(response).jsonObject
                return ApiResult.Success(response, rawResponse)
            }

            ClientMetadataValue.SIGN_IN_TYPE_GOOGLE -> {
                val response = AuthChallengeResponse(
                    authFlow = request.authFlow,
                    challengeName = AuthChallengeName.VERIFY_SSO,
                    session = UUID.randomUUID().toString(),
                    challengeParameters = buildJsonObject {
                        putTimestamp()
                        put(
                            ChallengeParametersKey.CLIENT_ID,
                            "644565990183-c7pm488lsmmqh2dqterc64u4q6hnl3cs.apps.googleusercontent.com"
                        )
                        put(
                            ChallengeParametersKey.APP_CLIENT_ID,
                            "644565990183-ht65k84e4fubsgb8ckv3uo0utc8qcaom.apps.googleusercontent.com"
                        )
                        put(ChallengeParametersKey.STATE, UUID.randomUUID().toString())
                    }
                )
                val rawResponse = JSON.encodeToJsonElement(response).jsonObject
                return ApiResult.Success(response, rawResponse)
            }

            else -> ApiResult.Exception(
                IllegalArgumentException("Invalid type: $signInType")
            )
        }
    }

    private fun getVerifyResponse(
        request: AuthChallengeRequest
    ): ApiResult<AuthChallengeResponse> {
        val nextChallengeName =
            request.clientMetadata?.get(ClientMetadataKey.MOCK_NEXT_CHALLENGE_NAME)
                ?.jsonPrimitive?.content ?: AuthChallengeName.SUCCESS

        val response = AuthChallengeResponse(
            authFlow = request.authFlow,
            challengeName = nextChallengeName,
            session = request.session,
            challengeParameters = buildJsonObject {
                putTimestamp()
                if (AuthChallengeName.VERIFY_OTP == nextChallengeName) {
                    putExpiryTime()
                }
            }
        )
        val rawResponse = JSON.encodeToJsonElement(response).jsonObject
        return ApiResult.Success(response, rawResponse)
    }

    private fun JsonObjectBuilder.putTimestamp(): JsonElement? =
        put(
            ChallengeParametersKey.TIMESTAMP,
            System.currentTimeMillis()
        )

    private fun JsonObjectBuilder.putExpiryTime(): JsonElement? =
        put(
            ChallengeParametersKey.EXPIRY_TIME,
            "${Random.nextInt(15, 60)} seconds"
        )
}