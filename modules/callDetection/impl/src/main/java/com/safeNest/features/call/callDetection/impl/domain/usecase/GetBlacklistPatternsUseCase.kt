package com.safeNest.features.call.callDetection.impl.domain.usecase

import com.safeNest.features.call.callDetection.impl.domain.repository.BlacklistPatternRepository
import javax.inject.Inject

class GetBlacklistPatternsUseCase @Inject constructor(private val repo: BlacklistPatternRepository) {
    operator fun invoke() = repo.getBlacklistPatterns()
}