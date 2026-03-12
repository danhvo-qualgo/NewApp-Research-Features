package com.safeNest.demo.call.main.impl.domain.usecase

import com.safeNest.demo.call.main.impl.domain.repository.BlacklistPatternRepository
import javax.inject.Inject

class RemoveBlackListPatternUseCase @Inject constructor(private val repo: BlacklistPatternRepository) {
    suspend operator fun invoke(number: String) = repo.remove(number)
}