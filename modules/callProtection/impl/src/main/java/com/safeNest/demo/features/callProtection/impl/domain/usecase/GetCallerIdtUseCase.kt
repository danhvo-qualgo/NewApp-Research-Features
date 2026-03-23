package com.safeNest.demo.features.callProtection.impl.domain.usecase

import com.safeNest.demo.features.callProtection.api.domain.model.CallerIdInfo
import com.safeNest.demo.features.callProtection.api.domain.model.GetCallerIdInfoUseCase
import com.safeNest.demo.features.callProtection.impl.domain.common.normalizePhoneNumber
import com.safeNest.demo.features.callProtection.impl.domain.repository.CallDetectionRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class GetCallerIdUseCaseImpl  @Inject constructor(private val repo: CallDetectionRepository) : GetCallerIdInfoUseCase {
    override suspend fun invoke(phoneNumber: String): CallerIdInfo? {
        return repo.evaluatePhoneNumber(normalizePhoneNumber(phoneNumber)).first()
    }
}