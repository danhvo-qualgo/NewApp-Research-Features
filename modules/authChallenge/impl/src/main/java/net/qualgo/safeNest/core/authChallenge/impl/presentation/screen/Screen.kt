package net.qualgo.safeNest.core.authChallenge.impl.presentation.screen

import kotlinx.serialization.Serializable

internal sealed interface Screen {

    @Serializable
    data object VerifyOtp : Screen

    @Serializable
    data object VerifyOtpHelp : Screen

    @Serializable
    data object VerifySso : Screen
}