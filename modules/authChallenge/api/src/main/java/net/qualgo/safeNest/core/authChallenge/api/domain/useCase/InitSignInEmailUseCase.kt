package net.qualgo.safeNest.core.authChallenge.api.domain.useCase

import com.uney.core.utils.kotlin.result.DomainResult
import kotlinx.serialization.json.JsonObject

interface InitSignInEmailUseCase {

    suspend operator fun invoke(email: String): DomainResult<JsonObject, String>
}