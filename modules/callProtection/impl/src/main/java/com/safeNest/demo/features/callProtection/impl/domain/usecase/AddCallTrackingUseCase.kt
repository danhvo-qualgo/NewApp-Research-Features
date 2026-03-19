package com.safeNest.demo.features.callProtection.impl.domain.usecase

import com.safeNest.demo.features.callProtection.impl.domain.repository.CallTrackingRepository
import javax.inject.Inject

class AddCallTrackingUseCase @Inject constructor(private val repo: CallTrackingRepository) {
    suspend operator fun invoke(phoneNumber: String) {
        repo.add(phoneNumber)
    }
}