package com.safeNest.features.core.authChallenge.impl.domain.useCase

import com.safeNest.features.core.authChallenge.api.domain.useCase.InitSignInSsoUseCase
import com.safeNest.features.core.authChallenge.impl.domain.model.AuthChallenge
import com.safeNest.features.core.authChallenge.impl.domain.model.AuthChallengeFlow
import com.safeNest.features.core.authChallenge.impl.domain.model.AuthChallengeName
import com.safeNest.features.core.authChallenge.impl.domain.model.ClientMetadataKey
import com.safeNest.features.core.authChallenge.impl.domain.model.ClientMetadataValue
import com.safeNest.features.core.authChallenge.impl.domain.repository.AuthChallengeRepository
import com.uney.core.utils.kotlin.result.DomainResult
import com.uney.core.utils.kotlin.result.mapSuccess
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import javax.inject.Inject

internal class InitSignInSsoUseCaseImpl @Inject constructor(
    private val authChallengeRepository: AuthChallengeRepository
) : InitSignInSsoUseCase {

    override suspend fun invoke(): DomainResult<JsonObject, String> {
        val data = AuthChallenge(
            authFlow = AuthChallengeFlow.LOGIN,
            challengeName = AuthChallengeName.INIT_CHALLENGE,
            clientMetadata = buildJsonObject {
                put(ClientMetadataKey.TYPE, ClientMetadataValue.SIGN_IN_TYPE_GOOGLE)
            }
        )
        val result = authChallengeRepository.process(data)
        return result.mapSuccess { toJsonObject(it) }
    }
}