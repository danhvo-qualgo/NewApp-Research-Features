package net.qualgo.safeNest.core.signIn.impl.presentation.router

import android.content.Context
import net.qualgo.safeNest.core.signIn.api.presentation.router.SignInRouterConst
import net.qualgo.safeNest.core.signIn.impl.presentation.activity.SignInActivity
import com.uney.core.router.InternalRouter
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class SignInRouter @Inject constructor(
    @param:ApplicationContext
    private val context: Context
) : InternalRouter(
    context = context,
    host = SignInRouterConst.HOST,
    activityCls = SignInActivity::class
)