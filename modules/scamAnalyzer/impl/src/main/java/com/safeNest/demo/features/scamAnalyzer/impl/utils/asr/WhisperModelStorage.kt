package com.safeNest.demo.features.scamAnalyzer.impl.utils.asr

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

interface WhisperModelStorage {
    val modelFile: File
}

@Singleton
class AppWhisperModelStorage @Inject constructor(
    @ApplicationContext context: Context,
) : WhisperModelStorage {
    override val modelFile: File = File(context.filesDir, ".whisper/whisper-tiny.tflite")
}
