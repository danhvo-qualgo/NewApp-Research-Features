package com.safeNest.features.core.authChallenge.impl.domain.useCase

import com.safeNest.features.core.authChallenge.impl.domain.model.AuthChallenge
import com.safeNest.features.core.authChallenge.impl.domain.repository.AuthChallengeRepository
import com.uney.core.coreutils.kotlin.model.DomainResult
import javax.inject.Inject

internal class ProcessAuthChallengeUseCase @Inject constructor(
    private val authChallengeRepository: AuthChallengeRepository
) {

    suspend operator fun invoke(data: AuthChallenge): DomainResult<AuthChallenge, String> {
        return authChallengeRepository.process(data)
    }
}