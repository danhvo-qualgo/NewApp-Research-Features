package com.safeNest.demo.features.callProtection.api.domain.model

interface GetCallerIdInfoUseCase {
    operator fun invoke(phoneNumber: String): CallerIdInfo
}