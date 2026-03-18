package com.safeNest.demo.features.designSystem.theme.color

import androidx.compose.ui.graphics.Color

interface TokenColor {
    val successLightest: Color
    val successLighter : Color
    val successLight: Color
    val success: Color
    val successDark: Color
    val successDarker: Color
    val successDarkest: Color
    val successDisable: Color

    val warningLightest: Color
    val warningLighter: Color
    val warningLight: Color
    val warning: Color
    val warningDark: Color
    val warningDarker: Color
    val warningDarkest: Color

    val errorLightest: Color
    val errorLighter: Color
    val errorLight: Color
    val error: Color
    val errorDark: Color
    val errorDarker: Color
    val errorDarkest: Color

    val infoLightest: Color
    val infoLighter: Color
    val infoLight: Color
    val info: Color
    val infoDark: Color
    val infoDarker: Color
    val infoDarkest: Color

    val linkLightest: Color
    val linkLighter: Color
    val linkLight: Color
    val link: Color
    val linkDark: Color
    val linkDarker: Color
    val linkDarkest: Color

    val white: Color
    val black: Color

    val neutralLightest: Color
    val neutralLighter: Color
    val neutralLight: Color
    val neutral: Color
    val neutralDark: Color
    val neutralDarker: Color
    val neutralDarkest: Color

    val primaryLightest: Color
    val primaryLighter: Color
    val primaryLight: Color
    val primary: Color
    val primaryDark: Color
    val primaryDarker: Color
    val primaryDarkest: Color

    val secondaryLightest: Color
    val secondaryLighter: Color
    val secondaryLight: Color
    val secondary: Color
    val secondaryDark: Color
    val secondaryDarker: Color
    val secondaryDarkest: Color

    // ── Scam Analyzer ──────────────────────────────────────────────────────────
    val scamSurface: Color
    val scamSurfaceDark: Color
    val neutralDarkestAlt: Color
    val neutralMuted: Color
    val cardSurface: Color
    val neutralLightest2: Color
    val gradientStart: Color
}

data object TokenColorLight : TokenColor {
    override val successLightest = colorTeal50
    override val successLighter = colorTeal200
    override val successLight = colorTeal400
    override val success = colorTeal600
    override val successDark = colorTeal700
    override val successDarker = colorTeal800
    override val successDarkest = colorTeal900
    override val successDisable = colorTeal200

    override val warningLightest = colorAmber50
    override val warningLighter = colorAmber200
    override val warningLight = colorAmber400
    override val warning = colorAmber600
    override val warningDark = colorAmber700
    override val warningDarker = colorAmber800
    override val warningDarkest = colorAmber900

    override val errorLightest = colorRed50
    override val errorLighter = colorRed200
    override val errorLight = colorRed400
    override val error = colorRed500
    override val errorDark = colorRed700
    override val errorDarker = colorRed800
    override val errorDarkest = colorRed900

    override val infoLightest = colorBlue50
    override val infoLighter = colorBlue200
    override val infoLight = colorBlue400
    override val info = colorBlue600
    override val infoDark = colorBlue700
    override val infoDarker = colorBlue800
    override val infoDarkest = colorBlue900

    override val linkLightest = colorBlue50
    override val linkLighter = colorBlue200
    override val linkLight = colorBlue400
    override val link = colorBlue600
    override val linkDark = colorBlue700
    override val linkDarker = colorBlue800
    override val linkDarkest = colorBlue900

    override val white = colorWhite
    override val black = colorBlack

    override val neutralLightest = colorGray50
    override val neutralLighter = colorGray200
    override val neutralLight = colorGray400
    override val neutral = colorGray600
    override val neutralDark = colorGray700
    override val neutralDarker = colorGray800
    override val neutralDarkest = colorGray900

    override val primaryLightest = colorIndigo50
    override val primaryLighter = colorIndigo200
    override val primaryLight = colorIndigo400
    override val primary = colorIndigo600
    override val primaryDark = colorIndigo700
    override val primaryDarker = colorIndigo800
    override val primaryDarkest = colorIndigo900

    override val secondaryLightest = colorOrange50
    override val secondaryLighter = colorOrange200
    override val secondaryLight = colorOrange400
    override val secondary = colorOrange600
    override val secondaryDark = colorOrange700
    override val secondaryDarker = colorOrange800
    override val secondaryDarkest = colorOrange900

    // ── Scam Analyzer ──────────────────────────────────────────────────────────
    override val scamSurface = colorScamSurface
    override val scamSurfaceDark = colorScamSurfaceDark
    override val neutralDarkestAlt = colorNeutral900
    override val neutralMuted = colorGray500
    override val cardSurface = colorWhiteCard
    override val neutralLightest2 = colorGray100
    override val gradientStart = colorIndigoGradientStart
}