package com.safeNest.demo.features.callProtection.impl.domain.usecase

import com.safeNest.demo.features.callProtection.impl.domain.model.BlacklistPattern
import com.safeNest.demo.features.callProtection.impl.domain.repository.BlacklistPatternRepository
import javax.inject.Inject

class AddBlacklistPatternUseCase @Inject constructor(private val repo: BlacklistPatternRepository) {
    suspend operator fun invoke(pattern: String, description: String) = repo.add(
        BlacklistPattern(
            pattern = pattern,
            description = description
        )
    )
}