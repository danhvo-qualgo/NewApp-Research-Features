package net.qualgo.safeNest.core.authChallenge.api.domain.useCase

import com.uney.core.utils.kotlin.result.DomainResult
import kotlinx.serialization.json.JsonObject

interface InitSignInSsoUseCase {

    suspend operator fun invoke(): DomainResult<JsonObject, String>
}