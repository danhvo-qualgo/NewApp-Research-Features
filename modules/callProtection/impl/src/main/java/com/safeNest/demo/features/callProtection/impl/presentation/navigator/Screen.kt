package com.safeNest.demo.features.callProtection.impl.presentation.navigator

import com.safeNest.demo.features.callProtection.api.domain.model.CallerIdInfo
import kotlinx.serialization.Serializable

@Serializable
sealed interface Screen {
    @Serializable
    data class Home(val page: String) : Screen

    @Serializable
    object AddBlocklist : Screen

    @Serializable
    object AddWhitelist : Screen

    @Serializable
    data class MissingCall(val callerIdInfo: CallerIdInfo) : Screen

    @Serializable
    data class  MakeCallConfirm(val callerIdInfo: CallerIdInfo) : Screen

    @Serializable
    data class  ReviewCall(val callerIdInfo: CallerIdInfo) : Screen
}