package com.safeNest.features.call.callDetection.impl.domain.usecase

import com.safeNest.features.call.callDetection.impl.domain.repository.BlacklistPatternRepository
import javax.inject.Inject

class AddBlacklistPatternUseCase @Inject constructor(private val repo: BlacklistPatternRepository) {
    suspend operator fun invoke(pattern: String) = repo.add(pattern)
}