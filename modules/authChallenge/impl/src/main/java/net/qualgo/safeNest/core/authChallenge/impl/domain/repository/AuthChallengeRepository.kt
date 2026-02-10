package net.qualgo.safeNest.core.authChallenge.impl.domain.repository

import net.qualgo.safeNest.core.authChallenge.impl.domain.model.AuthChallenge
import com.uney.core.utils.kotlin.result.DomainResult

interface AuthChallengeRepository {

    suspend fun process(data: AuthChallenge): DomainResult<AuthChallenge, String>
}