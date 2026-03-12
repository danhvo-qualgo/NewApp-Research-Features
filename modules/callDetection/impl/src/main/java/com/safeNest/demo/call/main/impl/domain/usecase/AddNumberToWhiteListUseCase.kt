package com.safeNest.demo.call.main.impl.domain.usecase

import com.safeNest.demo.call.main.impl.domain.repository.WhitelistRepository
import javax.inject.Inject

class AddNumberToWhiteListUseCase @Inject constructor(private val repo: WhitelistRepository) {
    suspend operator fun invoke(number: String) = repo.add(number)
}