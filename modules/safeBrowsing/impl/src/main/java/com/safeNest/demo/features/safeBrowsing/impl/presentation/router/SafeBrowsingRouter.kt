package com.safeNest.demo.features.safeBrowsing.impl.presentation.router

import android.content.Context
import com.safeNest.demo.features.safeBrowsing.api.router.SafeBrowsingRouterConst
import com.safeNest.demo.features.safeBrowsing.impl.presentation.SafeBrowsingActivity
import com.uney.core.router.InternalRouter
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class SafeBrowsingRouter @Inject constructor(
    @param:ApplicationContext
    private val context: Context
) : InternalRouter(context, SafeBrowsingRouterConst.HOST, SafeBrowsingActivity::class)