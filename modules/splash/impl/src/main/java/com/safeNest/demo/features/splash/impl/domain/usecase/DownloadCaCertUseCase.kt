package com.safeNest.demo.features.splash.impl.domain.usecase

import com.safeNest.demo.features.splash.impl.domain.repository.SplashRepository
import java.io.InputStream
import javax.inject.Inject

class DownloadCaCertUseCase @Inject constructor(
    private val repository: SplashRepository
) {
    suspend operator fun invoke(): Result<InputStream> = repository.downloadCaCert()
}