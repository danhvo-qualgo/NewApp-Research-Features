package com.safeNest.demo.call.main.impl.domain.usecase

import com.safeNest.demo.call.main.impl.domain.repository.BlacklistPatternRepository
import javax.inject.Inject

class EnableBlackListUseCase @Inject constructor(private val repo: BlacklistPatternRepository) {
    suspend operator fun invoke(isEnable: Boolean) = repo.setEnable(isEnable)
    fun isEnable() = repo.isEnable()
}