package com.safeNest.demo.features.urlGuard.api.useCase

import kotlinx.coroutines.flow.Flow

interface ManageFormCheckUseCase {
    suspend fun setEnabled(enabled: Boolean)
    suspend fun isEnabled(): Boolean
    fun observeEnabled(): Flow<Boolean>
}
