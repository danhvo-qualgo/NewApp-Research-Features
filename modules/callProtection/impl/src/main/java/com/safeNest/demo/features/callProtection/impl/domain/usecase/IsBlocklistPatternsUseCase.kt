package com.safeNest.demo.features.callProtection.impl.domain.usecase

import com.safeNest.demo.features.callProtection.impl.domain.repository.BlacklistPatternRepository
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class IsBlocklistPatternsUseCase @Inject constructor(private val repo: BlacklistPatternRepository) {
    operator fun invoke(number: String) = repo.getBlacklistPatterns().map {
        it.any { pattern ->
            number.startsWith(pattern.pattern)
        }
    }
}