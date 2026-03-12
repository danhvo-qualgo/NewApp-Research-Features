package com.safeNest.demo.features.call.impl.domain.usecase

import com.safeNest.demo.features.call.impl.domain.repository.WhitelistRepository
import javax.inject.Inject

class GetWhiteListUseCase @Inject constructor(private val repo: WhitelistRepository) {
    operator fun invoke() = repo.getWhitelist()
}