package com.safeNest.demo.features.callProtection.impl.domain.usecase

import com.safeNest.demo.features.callProtection.impl.domain.common.normalizePhoneNumber
import com.safeNest.demo.features.callProtection.impl.domain.repository.MasterWhitelistRepository
import javax.inject.Inject

class GetMasterWhitelistNumberUseCase @Inject constructor(private val repo: MasterWhitelistRepository) {
    suspend operator fun invoke(number: String) = normalizePhoneNumber(number).let {
        repo.getPhoneNumber(it)
    }
}