package net.qualgo.safeNest.core.authChallenge.impl.presentation.router

import android.content.Context
import net.qualgo.safeNest.core.authChallenge.api.presentation.router.AuthChallengeRouterConst
import net.qualgo.safeNest.core.authChallenge.impl.presentation.activity.AuthChallengeActivity
import com.uney.core.router.InternalRouter
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class AuthChallengeRouter @Inject constructor(
    @param:ApplicationContext
    private val context: Context
) : InternalRouter(
    context = context,
    host = AuthChallengeRouterConst.HOST,
    activityCls = AuthChallengeActivity::class
)