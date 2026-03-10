package net.qualgo.safeNest.features.phishingDetection.impl.presentation

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

interface ModelStorage {
    val modelDir: File
}

@Singleton
class AppModelStorage @Inject constructor(
    @ApplicationContext context: Context
) : ModelStorage {
    override val modelDir: File = File(context.filesDir, ".mnnmodels/Qwen3.5-2B-MNN")
}
