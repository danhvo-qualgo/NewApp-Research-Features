package com.safeNest.demo.features.callProtection.impl.domain.usecase

import com.safeNest.demo.features.callProtection.impl.domain.repository.MasterWhitelistRepository
import javax.inject.Inject

class GetMasterWhiteListUseCase @Inject constructor(private val repo: MasterWhitelistRepository) {
    operator fun invoke() = repo.getWhitelist()
}