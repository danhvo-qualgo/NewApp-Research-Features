package com.safeNest.features.core.authChallenge.api.domain.useCase

import com.uney.core.coreutils.kotlin.model.DomainResult
import kotlinx.serialization.json.JsonObject

interface InitSignInSsoUseCase {

    suspend operator fun invoke(): DomainResult<JsonObject, String>
}