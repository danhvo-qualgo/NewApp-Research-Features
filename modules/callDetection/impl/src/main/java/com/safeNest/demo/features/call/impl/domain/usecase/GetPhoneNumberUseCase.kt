package com.safeNest.demo.features.call.impl.domain.usecase

import com.safeNest.demo.features.call.impl.domain.repository.WhitelistRepository
import javax.inject.Inject

class GetPhoneNumberUseCase @Inject constructor(private val repo: WhitelistRepository) {
    suspend operator fun invoke(number: String) = repo.getPhoneNumber(number)
}