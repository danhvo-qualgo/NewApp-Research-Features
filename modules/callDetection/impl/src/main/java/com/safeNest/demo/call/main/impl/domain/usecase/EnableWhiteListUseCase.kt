package com.safeNest.demo.call.main.impl.domain.usecase

import com.safeNest.demo.call.main.impl.domain.repository.WhitelistRepository
import javax.inject.Inject

class EnableWhiteListUseCase @Inject constructor(private val repo: WhitelistRepository) {
    suspend operator fun invoke(isEnable: Boolean) = repo.setEnable(isEnable)
    fun isEnable() = repo.isEnable()
}