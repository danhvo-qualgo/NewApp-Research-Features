package com.safeNest.demo.call.main.impl.domain.usecase

import com.safeNest.demo.call.main.impl.domain.repository.BlacklistPatternRepository
import javax.inject.Inject

class AddBlacklistPatternUseCase @Inject constructor(private val repo: BlacklistPatternRepository) {
    suspend operator fun invoke(pattern: String) = repo.add(pattern)
}