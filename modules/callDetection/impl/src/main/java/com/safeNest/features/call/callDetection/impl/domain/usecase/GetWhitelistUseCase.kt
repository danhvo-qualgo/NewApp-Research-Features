package com.safeNest.features.call.callDetection.impl.domain.usecase

import com.safeNest.features.call.callDetection.impl.domain.repository.WhitelistRepository
import javax.inject.Inject

class GetWhiteListUseCase @Inject constructor(private val repo: WhitelistRepository) {
    operator fun invoke() = repo.getWhitelist()
}