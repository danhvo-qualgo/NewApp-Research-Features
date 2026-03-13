package com.safeNest.demo.features.callProtection.impl.presentation.navigator

import kotlinx.serialization.Serializable

@Serializable
sealed interface Screen {
    @Serializable
    object Home : Screen

    @Serializable
    object AddBlocklist : Screen

    @Serializable
    object AddWhitelist : Screen
}