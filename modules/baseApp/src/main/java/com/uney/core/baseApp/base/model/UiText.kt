package com.uney.core.baseApp.base.model

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

sealed interface UiText {

    data object Empty : UiText
    class DynamicString(val value: String) : UiText
    class StringResourceId(@field:StringRes val id: Int, val args: Array<Any> = arrayOf()) : UiText

    fun asString(context: Context): String {
        return when (this) {
            is Empty -> ""
            is DynamicString -> value
            is StringResourceId -> context.getString(id, *args)
        }
    }

    @Composable
    fun asString(): String {
        return asString(LocalContext.current)
    }
}