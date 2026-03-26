package com.safeNest.demo.features.splash.impl.domain.repository

import java.io.InputStream

interface SplashRepository {
    suspend fun downloadCaCert(): Result<InputStream>
}