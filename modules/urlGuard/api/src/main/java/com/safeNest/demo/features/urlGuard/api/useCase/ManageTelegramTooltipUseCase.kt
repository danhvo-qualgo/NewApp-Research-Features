package com.safeNest.demo.features.urlGuard.api.useCase

interface ManageTelegramTooltipUseCase {
    suspend fun increaseCount()
    suspend fun getCount(): Int
    suspend fun resetCount()
}