package com.safeNest.demo.features.callProtection.impl.domain.usecase

import com.safeNest.demo.features.callProtection.impl.domain.repository.CallTrackingRepository
import javax.inject.Inject

class GetCallTrackingUseCase @Inject constructor(private val repo: CallTrackingRepository) {
    suspend operator fun invoke(phoneNumber: String) = repo.get(phoneNumber)
}