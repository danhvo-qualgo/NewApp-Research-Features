package com.safeNest.demo.features.commonUseCases.api

import com.uney.core.utils.kotlin.result.DomainResult

interface TestApiUseCase {
    suspend operator fun invoke(): DomainResult<String, String>
}