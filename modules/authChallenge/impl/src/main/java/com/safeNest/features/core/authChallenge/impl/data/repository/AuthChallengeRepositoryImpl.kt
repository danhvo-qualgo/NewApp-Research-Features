package com.safeNest.features.core.authChallenge.impl.data.repository

import com.safeNest.features.core.authChallenge.impl.domain.model.AuthChallenge
import com.safeNest.features.core.authChallenge.impl.ApiResult
import com.safeNest.features.core.authChallenge.impl.data.source.AuthChallengeRequest
import com.safeNest.features.core.authChallenge.impl.data.source.AuthChallengeResponse
import com.safeNest.features.core.authChallenge.impl.data.source.MockRemoteSource
import com.safeNest.features.core.authChallenge.impl.domain.repository.AuthChallengeRepository
import com.uney.core.coreutils.kotlin.model.DomainResult
import jakarta.inject.Inject

class AuthChallengeRepositoryImpl @Inject constructor() : AuthChallengeRepository {

    override suspend fun process(
        data: AuthChallenge
    ): DomainResult<AuthChallenge, String> {
        val request = AuthChallengeRequest(
            authFlow = data.authFlow,
            challengeName = data.challengeName,
            clientMetadata = data.clientMetadata,
            session = data.session,
            challengeResponses = data.challengeResponses
        )
        return when (val response = MockRemoteSource.processAuthChallenge(request)) {
            is ApiResult.Error -> {
                DomainResult.Error(response.errorMessage)
            }

            is ApiResult.Exception -> {
                DomainResult.Error(response.exception.message ?: "Unknown error")
            }

            is ApiResult.Success<AuthChallengeResponse> -> {
                val result = AuthChallenge(
                    authFlow = response.data.authFlow,
                    challengeName = response.data.challengeName,
                    session = response.data.session,
                    challengeParameters = response.data.challengeParameters,
                    challengeResponses = response.rawResponse
                )
                DomainResult.Success(result)
            }
        }
    }
}