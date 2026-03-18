package com.safeNest.demo.features.commonUseCases.impl.domain.repository

import com.uney.core.utils.kotlin.result.DomainResult

interface TestApiRepository {

    suspend fun callApi(): DomainResult<String, String>
}