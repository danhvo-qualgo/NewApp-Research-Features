package com.safeNest.demo.features.scamAnalyzer.api.useCase

interface ManageCustomPromptUseCase {
    suspend fun getCustomPrompt(): String
    suspend fun setCustomPrompt(prompt: String)
    suspend fun getDefaultPrompt(): String
}
