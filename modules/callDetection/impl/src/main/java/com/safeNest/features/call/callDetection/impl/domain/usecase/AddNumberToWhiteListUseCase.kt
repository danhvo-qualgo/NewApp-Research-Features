package com.safeNest.features.call.callDetection.impl.domain.usecase

import com.safeNest.features.call.callDetection.impl.domain.model.WhitelistNumber
import com.safeNest.features.call.callDetection.impl.domain.repository.WhitelistRepository
import javax.inject.Inject

class AddNumberToWhiteListUseCase @Inject constructor(private val repo: WhitelistRepository) {
    suspend operator fun invoke(number: String, name: String) = repo.add(WhitelistNumber(phoneNumber = number, name = name))
}