package com.safeNest.demo.features.callProtection.impl.domain.usecase
import com.safeNest.demo.features.callProtection.impl.domain.common.normalizePhoneNumber
import com.safeNest.demo.features.callProtection.impl.domain.model.PhoneNumberInfo
import com.safeNest.demo.features.callProtection.impl.domain.repository.MasterBlocklistRepository
import javax.inject.Inject

class AddToMasterBlockListUseCase @Inject constructor(private val repo: MasterBlocklistRepository) {
    suspend operator fun invoke(number: String, name: String) = repo.add(
        PhoneNumberInfo(
            phoneNumber = number,
            name = name,
            normalizedNumber = normalizePhoneNumber(number)
        )
    )
}