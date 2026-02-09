package com.uney.core.baseApp.base.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import com.uney.core.router.RouterManager
import com.uney.core.router.compose.LocalRouterManager
import javax.inject.Inject

open class BaseActivity : ComponentActivity() {

    @Inject
    lateinit var routerManager: RouterManager

    @Inject
    internal lateinit var activityDelegate: DefaultActivityDelegate

    protected val delegate by lazy { createActivityDelegate() }

    @Deprecated(
        "Don't override BaseActivity#onCreate",
        replaceWith = ReplaceWith("BaseActivity#onCreateContent"),
        level = DeprecationLevel.ERROR,
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        onCreateContent(savedInstanceState)

        setContent {
            CompositionLocalProvider(
                LocalRouterManager provides routerManager
            ) {
                Content(savedInstanceState)
            }
        }
    }

    open fun onCreateContent(savedInstanceState: Bundle?) = Unit

    @Composable
    open fun Content(savedInstanceState: Bundle?) = Unit

    open fun createActivityDelegate(): ActivityDelegate {
        return activityDelegate
    }
}