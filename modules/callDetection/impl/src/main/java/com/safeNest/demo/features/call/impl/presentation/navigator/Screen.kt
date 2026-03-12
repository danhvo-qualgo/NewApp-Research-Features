package com.safeNest.demo.features.call.impl.presentation.navigator

import kotlinx.serialization.Serializable

@Serializable
sealed interface Screen {
    @Serializable
    object Home : Screen

    @Serializable
    object Whitelist : Screen

    @Serializable
    object Blacklist : Screen
}