package com.safeNest.demo.call.main.impl.domain.usecase

import com.safeNest.demo.call.main.impl.domain.repository.BlacklistPatternRepository
import javax.inject.Inject

class GetBlacklistPatternsUseCase @Inject constructor(private val repo: BlacklistPatternRepository) {
    operator fun invoke() = repo.getBlacklistPatterns()
}