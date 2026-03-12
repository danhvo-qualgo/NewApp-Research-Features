package com.safeNest.demo.call.main.impl.domain.usecase

import com.safeNest.demo.call.main.impl.domain.repository.WhitelistRepository
import javax.inject.Inject

class GetWhiteListUseCase @Inject constructor(private val repo: WhitelistRepository) {
    operator fun invoke() = repo.getWhitelist()
}