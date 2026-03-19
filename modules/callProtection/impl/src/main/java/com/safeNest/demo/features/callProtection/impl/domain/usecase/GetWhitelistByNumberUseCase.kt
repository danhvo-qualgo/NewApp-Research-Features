package com.safeNest.demo.features.callProtection.impl.domain.usecase

import com.safeNest.demo.features.callProtection.impl.domain.repository.WhitelistRepository
import javax.inject.Inject

class GetWhitelistByNumberUseCase @Inject constructor(private val repo: WhitelistRepository) {
    operator fun invoke(number: String) = repo.getPhoneNumber(number)
}