package com.safeNest.demo.features.callProtection.impl.domain.usecase

import com.safeNest.demo.features.callProtection.impl.domain.repository.MasterBlocklistRepository
import javax.inject.Inject

class GetMasterBlocklistUseCase @Inject constructor(private val repo: MasterBlocklistRepository) {
    operator fun invoke() = repo.getBlocklist()
}