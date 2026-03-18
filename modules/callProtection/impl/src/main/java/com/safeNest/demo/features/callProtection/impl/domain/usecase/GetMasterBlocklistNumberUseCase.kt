package com.safeNest.demo.features.callProtection.impl.domain.usecase

import com.safeNest.demo.features.callProtection.impl.domain.common.normalizePhoneNumber
import com.safeNest.demo.features.callProtection.impl.domain.repository.MasterBlocklistRepository
import javax.inject.Inject

class GetMasterBlocklistNumberUseCase @Inject constructor(private val repo: MasterBlocklistRepository) {
    suspend operator fun invoke(number: String) = normalizePhoneNumber(number).let {
        repo.getPhoneNumber(it)
    }
}