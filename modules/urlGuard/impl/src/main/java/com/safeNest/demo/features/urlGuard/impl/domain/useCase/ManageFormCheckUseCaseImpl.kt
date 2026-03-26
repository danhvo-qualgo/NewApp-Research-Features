package com.safeNest.demo.features.urlGuard.impl.domain.useCase

import com.safeNest.demo.features.urlGuard.api.useCase.ManageFormCheckUseCase
import com.safeNest.demo.features.urlGuard.impl.data.store.UrlGuardStore
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ManageFormCheckUseCaseImpl @Inject constructor(
    private val store: UrlGuardStore
) : ManageFormCheckUseCase {

    override suspend fun setEnabled(enabled: Boolean) {
        store.setEnableFormCheck(enabled)
    }

    override suspend fun isEnabled(): Boolean {
        return store.isFormCheckEnabled()
    }

    override fun observeEnabled(): Flow<Boolean> {
        return store.observeFormCheckEnabled()
    }
}
