package com.safeNest.features.call.callDetection.impl.domain.usecase

import com.safeNest.features.call.callDetection.impl.domain.repository.BlacklistPatternRepository
import com.safeNest.features.call.callDetection.impl.domain.repository.WhitelistRepository
import javax.inject.Inject

class EnableBlackListUseCase @Inject constructor(private val repo: BlacklistPatternRepository) {
    suspend operator fun invoke(isEnable: Boolean) = repo.setEnable(isEnable)
    fun isEnable() = repo.isEnable()
}