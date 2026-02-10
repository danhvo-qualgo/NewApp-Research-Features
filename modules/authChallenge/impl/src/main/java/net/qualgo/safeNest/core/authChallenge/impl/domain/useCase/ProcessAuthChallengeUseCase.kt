package net.qualgo.safeNest.core.authChallenge.impl.domain.useCase

import net.qualgo.safeNest.core.authChallenge.impl.domain.model.AuthChallenge
import net.qualgo.safeNest.core.authChallenge.impl.domain.repository.AuthChallengeRepository
import com.uney.core.utils.kotlin.result.DomainResult
import javax.inject.Inject

internal class ProcessAuthChallengeUseCase @Inject constructor(
    private val authChallengeRepository: AuthChallengeRepository
) {

    suspend operator fun invoke(data: AuthChallenge): DomainResult<AuthChallenge, String> {
        return authChallengeRepository.process(data)
    }
}