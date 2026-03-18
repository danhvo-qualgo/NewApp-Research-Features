package com.safeNest.demo.features.callProtection.impl.domain.usecase

import com.safeNest.demo.features.callProtection.api.domain.model.CallerIdInfo
import com.safeNest.demo.features.callProtection.api.domain.model.CallerIdInfoType
import com.safeNest.demo.features.callProtection.api.domain.model.GetCallerIdInfoUseCase
import com.safeNest.demo.features.callProtection.impl.domain.repository.WhitelistRepository
import javax.inject.Inject

class GetCallerIdUseCaseImpl  @Inject constructor(private val repo: WhitelistRepository) : GetCallerIdInfoUseCase {
    override fun invoke(phoneNumber: String): CallerIdInfo {
        return CallerIdInfo(
            phoneNumber,
            CallerIdInfoType.SAFE
        )
    }
}