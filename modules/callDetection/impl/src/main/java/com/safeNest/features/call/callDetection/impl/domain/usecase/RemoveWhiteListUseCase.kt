package com.safeNest.features.call.callDetection.impl.domain.usecase

import com.safeNest.features.call.callDetection.impl.domain.repository.WhitelistRepository
import javax.inject.Inject

class RemoveWhiteListUseCase @Inject constructor(private val repo: WhitelistRepository) {
    suspend operator fun invoke(number: String) = repo.remove(number)
}