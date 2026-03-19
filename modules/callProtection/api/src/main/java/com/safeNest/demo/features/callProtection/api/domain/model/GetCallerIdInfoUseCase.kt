package com.safeNest.demo.features.callProtection.api.domain.model

interface GetCallerIdInfoUseCase {
    suspend operator fun invoke(phoneNumber: String): CallerIdInfo?
}