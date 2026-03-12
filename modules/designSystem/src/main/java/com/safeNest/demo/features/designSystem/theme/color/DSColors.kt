package com.safeNest.demo.features.designSystem.theme.color

import androidx.compose.runtime.Composable
import com.safeNest.demo.features.designSystem.theme.isInDarkTheme

object DSColors {
    val current: TokenColor
        @Composable
        get() = if (isInDarkTheme()) TokenColorLight else TokenColorLight

    val textBody @Composable get() = current.neutralDark
    val textHeading @Composable get() = current.neutralDarkest
    val textNeutral @Composable get() = current.neutralLight
    val textDisabled @Composable get() = current.neutralLighter
    val textAction @Composable get() = current.primary
    val textActionDisabled @Composable get() = current.primaryLighter
    val textActionActive @Composable get() = current.primaryDark
    val textOnAction @Composable get() = current.white
    val textOnDisabled @Composable get() = current.neutralDarker
    val textSuccess @Composable get() = current.success
    val textWarning @Composable get() = current.warning
    val textError @Composable get() = current.error
    val textInfo @Composable get() = current.info
    val textLink @Composable get() = current.link
    val textInverted @Composable get() = current.white
    val iconBody @Composable get() = current.neutral
    val iconHeading @Composable get() = current.neutralDarkest
    val iconNeutral @Composable get() = current.neutralLight
    val iconDisabled @Composable get() = current.neutralLighter
    val iconAction @Composable get() = current.primary
    val iconActionDisabled @Composable get() = current.primaryLighter
    val iconActionActive @Composable get() = current.primaryDark
    val iconOnAction @Composable get() = current.white
    val iconOnDisabled @Composable get() = current.neutralDarker
    val iconSuccess @Composable get() = current.success
    val iconWarning @Composable get() = current.warning
    val iconError @Composable get() = current.error
    val iconInfo @Composable get() = current.info
    val iconLink @Composable get() = current.link
    val iconInverted @Composable get() = current.white
    val iconStarStroke @Composable get() = current.neutralLight
    val iconStarActive @Composable get() = current.warningLight
    val surfacePrimary @Composable get() = current.white
    val surfaceGray @Composable get() = current.neutral
    val surfaceGrayLightest @Composable get() = current.neutralLightest
    val surfaceActive @Composable get() = current.primaryLightest
    val surfaceDisabled @Composable get() = current.neutralLightest
    val surfaceAction @Composable get() = current.primary
    val surfaceActionActive @Composable get() = current.primaryDark
    val surfaceActionDisabled @Composable get() = current.primaryLighter
    val surfaceSuccess @Composable get() = current.success
    val surfaceSuccessLightest @Composable get() = current.successLightest
    val surfaceWarning @Composable get() = current.warning
    val surfaceWarningLightest @Composable get() = current.warningLightest
    val surfaceError @Composable get() = current.error
    val surfaceErrorLightest @Composable get() = current.errorLightest
    val surfaceInfo @Composable get() = current.info
    val surfaceInfoLightest @Composable get() = current.infoLightest
    val surfaceLink @Composable get() = current.link
    val surfaceLoading @Composable get() = current.neutralLighter
    val surfaceSwitch @Composable get() = current.neutralLightest
    val surfaceNeutral @Composable get() = current.neutralDarker
    val surfaceDarkest @Composable get() = current.neutralDarkest
    val surfaceDivider @Composable get() = current.neutralLighter
    val surface1 @Composable get() = current.white
    val surface2 @Composable get() = current.neutralLightest
    val surface3 @Composable get() = current.neutralLighter
    val borderPrimary @Composable get() = current.neutralLightest
    val borderNeutral @Composable get() = current.neutralDark
    val borderSuccess @Composable get() = current.success
    val borderWarning @Composable get() = current.warning
    val borderError @Composable get() = current.error
    val borderInfo @Composable get() = current.info
    val borderHighlight @Composable get() = current.link
    val borderDisabled @Composable get() = current.neutralLightest
    val borderAction @Composable get() = current.primary
    val borderActionDisabled @Composable get() = current.primaryLighter
    val borderActionActive @Composable get() = current.primaryDark
    val borderInput @Composable get() = current.neutralLighter
    val borderInputFocus @Composable get() = current.primary
    val borderCheckbox @Composable get() = current.neutral
    val borderCheckboxFocus @Composable get() = current.primary
    val borderInverted @Composable get() = current.white
    val borderDarkest @Composable get() = current.neutralDarkest
    val borderButton @Composable get() = current.neutralLighter
}