package net.qualgo.safeNest.features.phishingDetection.impl.presentation.router

import android.content.Context
import com.uney.core.router.InternalRouter
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import net.qualgo.safeNest.features.phishingDetection.api.presentation.router.PhishingDetectionRouterConst
import net.qualgo.safeNest.features.phishingDetection.impl.presentation.PhishingDetectionActivity

class PhishingDetectionRouter @Inject constructor(
    @param:ApplicationContext
    private val context: Context
): InternalRouter(context, PhishingDetectionRouterConst.HOST, PhishingDetectionActivity::class)