package com.safeNest.demo.features.scamAnalyzer.impl.presentation.router

import android.content.Context
import com.safeNest.demo.features.scamAnalyzer.api.router.ScamAnalyzerRouterConst
import com.safeNest.demo.features.scamAnalyzer.impl.presentation.ScamAnalyzerActivity
import com.uney.core.router.InternalRouter
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class ScamAnalyzerRouter @Inject constructor(
    @param:ApplicationContext
    private val context: Context
) : InternalRouter(context, ScamAnalyzerRouterConst.HOST, ScamAnalyzerActivity::class)