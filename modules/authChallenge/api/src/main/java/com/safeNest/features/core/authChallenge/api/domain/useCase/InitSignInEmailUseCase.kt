package com.safeNest.features.core.authChallenge.api.domain.useCase

import com.uney.core.coreutils.kotlin.model.DomainResult
import kotlinx.serialization.json.JsonObject

interface InitSignInEmailUseCase {

    suspend operator fun invoke(email: String): DomainResult<JsonObject, String>
}