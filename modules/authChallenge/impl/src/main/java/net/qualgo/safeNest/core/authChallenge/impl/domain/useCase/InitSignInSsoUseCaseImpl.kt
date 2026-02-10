package net.qualgo.safeNest.core.authChallenge.impl.domain.useCase

import net.qualgo.safeNest.core.authChallenge.api.domain.useCase.InitSignInSsoUseCase
import net.qualgo.safeNest.core.authChallenge.impl.domain.model.AuthChallenge
import net.qualgo.safeNest.core.authChallenge.impl.domain.model.AuthChallengeFlow
import net.qualgo.safeNest.core.authChallenge.impl.domain.model.AuthChallengeName
import net.qualgo.safeNest.core.authChallenge.impl.domain.model.ClientMetadataKey
import net.qualgo.safeNest.core.authChallenge.impl.domain.model.ClientMetadataValue
import net.qualgo.safeNest.core.authChallenge.impl.domain.repository.AuthChallengeRepository
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