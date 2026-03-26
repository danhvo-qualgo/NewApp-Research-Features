package com.safeNest.demo.features.urlGuard.impl.domain.useCase

import com.safeNest.demo.features.urlGuard.api.useCase.ManageTelegramTooltipUseCase
import com.safeNest.demo.features.urlGuard.impl.data.store.UrlGuardStore
import javax.inject.Inject

class ManageTelegramTooltipUseCaseImpl @Inject constructor(
    private val urlGuardStore: UrlGuardStore
) : ManageTelegramTooltipUseCase {
    override suspend fun increaseCount() {
        urlGuardStore.increaseCountOpenTelegram()
    }

    override suspend fun getCount(): Int {
        return urlGuardStore.getCountOpenTelegram()
    }

    override suspend fun resetCount() {
        urlGuardStore.resetCount()
    }
}