package com.safeNest.demo.features.home.impl.presentation.ui.tool

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.safeNest.demo.features.scamAnalyzer.api.models.AnalysisInput
import com.safeNest.demo.features.scamAnalyzer.api.useCase.AnalyzeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ScamAnalyzerViewModel @Inject constructor(
    private val analyzeUseCase: AnalyzeUseCase
) : ViewModel() {
    fun analyzeText(text: String) {
        viewModelScope.launch {
            val result = analyzeUseCase(AnalysisInput.Text(text))
            Log.d("AnalyzeResult", result.toString())
        }
    }
}