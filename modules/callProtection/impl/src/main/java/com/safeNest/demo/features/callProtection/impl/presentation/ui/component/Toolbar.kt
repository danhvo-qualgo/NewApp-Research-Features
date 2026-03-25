package com.safeNest.demo.features.callProtection.impl.presentation.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.safeNest.demo.features.callProtection.impl.R
import com.safeNest.demo.features.designSystem.theme.DSSpacing
import com.safeNest.demo.features.designSystem.theme.DSTypography
import com.safeNest.demo.features.designSystem.theme.color.DSColors


@Composable
fun Toolbar(
    text: String,
    icon: ImageVector = ImageVector.vectorResource(R.drawable.ic_arrow_back),
    onActionClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(horizontal = DSSpacing.s6, vertical = DSSpacing.s4).fillMaxWidth()
    ) {
        Box(
            modifier = Modifier.padding(DSSpacing.s2).clickable {
                onActionClick()
            }
        ) {
            Icon(
                imageVector = icon,
                contentDescription = "Back",
                tint = DSColors.textAction,
                modifier = Modifier
                    .size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(DSSpacing.s4))
        Text(
            text = text,
            style = DSTypography.h4.bold,
            color = DSColors.textAction
        )
    }
}

@Preview
@Composable
fun ToolbarPreview() {
    Toolbar("Add Allowed Contact") {

    }
}