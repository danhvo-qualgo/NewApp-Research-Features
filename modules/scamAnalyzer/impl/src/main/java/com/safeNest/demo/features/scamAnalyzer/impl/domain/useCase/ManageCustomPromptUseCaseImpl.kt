package com.safeNest.demo.features.scamAnalyzer.impl.domain.useCase

import com.safeNest.demo.features.scamAnalyzer.api.useCase.ManageCustomPromptUseCase
import com.safeNest.demo.features.scamAnalyzer.impl.data.store.AnalyzeStore
import javax.inject.Inject

class ManageCustomPromptUseCaseImpl @Inject constructor(
    private val analyzeStore: AnalyzeStore
) : ManageCustomPromptUseCase {
    override suspend fun getCustomPrompt(): String {
        return analyzeStore.getCustomPrompt()
    }

    override suspend fun setCustomPrompt(prompt: String) {
        analyzeStore.setCustomPrompt(prompt)
    }

    override suspend fun getDefaultPrompt(): String {
        return AnalyzeStore.DEFAULT_PROMPT.trimIndent()
    }
}
