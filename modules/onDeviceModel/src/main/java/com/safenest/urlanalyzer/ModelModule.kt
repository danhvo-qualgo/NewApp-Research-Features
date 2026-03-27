package com.safenest.urlanalyzer

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ModelModule {

    /**
     * Override this binding in tests to point at a stub model or a smaller
     * GGUF that downloads quickly.
     */
    @Provides
    @Singleton
    fun provideModelConfig(): ModelConfig = ModelConfig()
}