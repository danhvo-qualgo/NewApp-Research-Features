package com.safeNest.demo.features.designSystem.component

import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import com.safeNest.demo.features.designSystem.theme.DSTypography
import com.safeNest.demo.features.designSystem.theme.color.DSColors


@Composable
fun DSButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    textStyle: TextStyle = DSTypography.caption1.medium,
    textColor: Color = DSColors.textOnAction,
    onClick: () -> Unit
) {
    Button(
        modifier = modifier,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors().copy(containerColor = DSColors.surfaceAction),
        onClick = onClick
    ) {
        Text(
            text = text,
            style = textStyle,
            color = textColor
        )
    }
}