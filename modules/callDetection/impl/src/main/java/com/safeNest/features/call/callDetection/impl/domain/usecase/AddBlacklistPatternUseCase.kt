package com.safeNest.features.call.callDetection.impl.domain.usecase

import com.safeNest.features.call.callDetection.impl.domain.model.BlacklistPattern
import com.safeNest.features.call.callDetection.impl.domain.repository.BlacklistPatternRepository
import javax.inject.Inject

class AddBlacklistPatternUseCase @Inject constructor(private val repo: BlacklistPatternRepository) {
    suspend operator fun invoke(pattern: String, description: String) = repo.add(BlacklistPattern(
        pattern = pattern,
        description = description
    ))
}