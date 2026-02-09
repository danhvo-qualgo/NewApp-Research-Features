package com.safeNest.features.core.authChallenge.impl.domain.repository

import com.safeNest.features.core.authChallenge.impl.domain.model.AuthChallenge
import com.uney.core.utils.kotlin.result.DomainResult

interface AuthChallengeRepository {

    suspend fun process(data: AuthChallenge): DomainResult<AuthChallenge, String>
}