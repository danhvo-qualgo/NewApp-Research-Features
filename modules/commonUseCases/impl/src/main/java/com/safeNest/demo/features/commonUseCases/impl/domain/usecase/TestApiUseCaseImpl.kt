package com.safeNest.demo.features.commonUseCases.impl.domain.usecase

import com.safeNest.demo.features.commonUseCases.api.TestApiUseCase
import com.safeNest.demo.features.commonUseCases.impl.domain.repository.TestApiRepository
import com.uney.core.utils.kotlin.result.DomainResult
import javax.inject.Inject

class TestApiUseCaseImpl @Inject constructor(
    private val testApiRepository: TestApiRepository
) : TestApiUseCase {

    override suspend fun invoke(): DomainResult<String, String> = testApiRepository.callApi()
}