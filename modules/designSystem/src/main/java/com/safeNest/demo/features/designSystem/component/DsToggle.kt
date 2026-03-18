package com.safeNest.demo.features.designSystem.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.safeNest.demo.features.designSystem.theme.DSTypography
import com.safeNest.demo.features.designSystem.theme.color.DSColors


@Composable
fun DsToggle(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    size: DsToggleSize = DsToggleSize.MD,
    enabled: Boolean = true,
    hint: String? = null
) {
    val interactionSource = remember { MutableInteractionSource() }

    DsToggleImpl(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier,
        label = label,
        size = size,
        enabled = enabled,
        hint = hint,
        interactionSource = interactionSource
    )
}

@Composable
private fun DsToggleImpl(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    size: DsToggleSize,
    enabled: Boolean,
    hint: String? = null,
    interactionSource: MutableInteractionSource,
) {
    val toggleColor by animateColorAsState(
        targetValue = when {
            !enabled -> if (checked) DSColors.surfaceDisabled else DSColors.surfaceDisabled // Disabled colors
            checked -> DSColors.surfaceAction
            else -> DSColors.surface3
        },
        animationSpec = tween(durationMillis = 200),
        label = "ToggleBackgroundColor"
    )

    val thumbOffset by animateDpAsState(
        targetValue = if (checked) size.width - size.thumbHeight - size.thumbPadding else size.thumbPadding,
        animationSpec = tween(durationMillis = 200),
        label = "ThumbOffset"
    )

    Row(
        modifier = modifier
            .toggleable(
                value = checked,
                onValueChange = onCheckedChange,
                enabled = enabled,
                role = Role.Switch,
                interactionSource = interactionSource,
                indication = null
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Toggle Switch
        Box(
            modifier = Modifier
                .size(width = size.width, height = size.height)
                .clip(RoundedCornerShape(999.dp))
                .background(toggleColor)
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = thumbOffset)
                    .size(size.thumbHeight)
                    .shadow(elevation = 1.dp, shape = CircleShape)
                    .background(Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (checked) {
                    Icon(
                        imageVector = CheckIcon,
                        contentDescription = null,
                        modifier = Modifier.size(size.thumbHeight * 0.6f),
                        tint = if(enabled) DSColors.surfaceAction else DSColors.surfaceAction
                    )
                }
            }
        }

        // Label and Hint
        if (label != null || hint != null) {
            Column(
                modifier = Modifier.padding(start = size.spacing),
                verticalArrangement = Arrangement.Center
            ) {
                if (label != null) {
                    Text(
                        text = label,
                        style = if (checked) size.labelStyle.copy(fontWeight = FontWeight.Medium) else size.labelStyle,
                        color = if (enabled) DSColors.textBody else DSColors.textDisabled
                    )
                }
                if (hint != null) {
                    Text(
                        text = hint,
                        style = size.hintStyle,
                        color = if (enabled) DSColors.textNeutral else DSColors.textDisabled
                    )
                }
            }
        }
    }
}

private val CheckIcon: ImageVector
    get() = ImageVector.Builder(
        name = "Check",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            fill = null,
            fillAlpha = 1.0f,
            stroke = SolidColor(Color.Black),
            strokeAlpha = 1.0f,
            strokeLineWidth = 3f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
            pathFillType = PathFillType.NonZero
        ) {
            moveTo(5f, 13f)
            lineTo(9f, 17f)
            lineTo(19f, 7f)
        }
    }.build()

enum class DsToggleSize(
    val width: Dp,
    val height: Dp,
    val thumbHeight: Dp,
    val thumbPadding: Dp,
    val spacing: Dp,
    val labelStyle: TextStyle,
    val hintStyle: TextStyle
) {
    SM(
        width = 44.dp,
        height = 24.dp,
        thumbHeight = 20.dp,
        thumbPadding = 2.dp,
        spacing = 12.dp,
        labelStyle = DSTypography.caption1.regular,
        hintStyle = DSTypography.caption2Hint.regular
    ),
    MD(
        width = 54.dp,
        height = 32.dp,
        thumbHeight = 24.dp,
        thumbPadding = 4.dp,
        spacing = 16.dp,
        labelStyle = DSTypography.body2.regular,
        hintStyle = DSTypography.caption2.regular
    )
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, widthDp = 1200, heightDp = 1500)
@Composable
private fun DsTogglePreview() {
    Column(
        modifier = Modifier.padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(32.dp)
    ) {
        val states = listOf("Default", "Disabled")
        val sizes = listOf(DsToggleSize.SM, DsToggleSize.MD)

        sizes.forEach { size ->
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Size: ${size.name}", style = DSTypography.h4.bold)

                // Grid Headings
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Box(Modifier.width(80.dp)) // State Label

                    // No Label
                    Text("No Label (Off)", style = DSTypography.caption1.bold, modifier = Modifier.width(100.dp))
                    Text("No Label (On)", style = DSTypography.caption1.bold, modifier = Modifier.width(100.dp))

                    // Label
                    Text("Label (Off)", style = DSTypography.caption1.bold, modifier = Modifier.width(130.dp))
                    Text("Label (On)", style = DSTypography.caption1.bold, modifier = Modifier.width(130.dp))

                    // Label + Hint
                    Text("Hint (Off)", style = DSTypography.caption1.bold, modifier = Modifier.width(130.dp))
                    Text("Hint (On)", style = DSTypography.caption1.bold, modifier = Modifier.width(130.dp))
                }

                // States
                states.forEach { state ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // State Label
                        Text(
                            text = state,
                            style = DSTypography.body2.medium,
                            modifier = Modifier.width(80.dp)
                        )

                        // 1. No Label - Off
                        Box(Modifier.width(100.dp)) {
                            PreviewToggle(checked = false, state = state, size = size, mode = TogglePreviewMode.NoLabel)
                        }
                        // 2. No Label - On
                        Box(Modifier.width(100.dp)) {
                            PreviewToggle(checked = true, state = state, size = size, mode = TogglePreviewMode.NoLabel)
                        }

                        // 3. Label - Off
                        Box(Modifier.width(130.dp)) {
                            PreviewToggle(checked = false, state = state, size = size, mode = TogglePreviewMode.Label)
                        }
                        // 4. Label - On
                        Box(Modifier.width(130.dp)) {
                            PreviewToggle(checked = true, state = state, size = size, mode = TogglePreviewMode.Label)
                        }

                        // 5. Label + Hint - Off
                        Box(Modifier.width(130.dp)) {
                            PreviewToggle(checked = false, state = state, size = size, mode = TogglePreviewMode.LabelWithHint)
                        }
                        // 6. Label + Hint - On
                        Box(Modifier.width(130.dp)) {
                            PreviewToggle(checked = true, state = state, size = size, mode = TogglePreviewMode.LabelWithHint)
                        }
                    }
                }
            }
        }
    }
}

private enum class TogglePreviewMode {
    NoLabel,
    Label,
    LabelWithHint
}

@Composable
private fun PreviewToggle(checked: Boolean, state: String, size: DsToggleSize, mode: TogglePreviewMode) {
    val enabled = state != "Disabled"

    val labelText = if (mode != TogglePreviewMode.NoLabel) "Label toggle" else null
    val hintText = if (mode == TogglePreviewMode.LabelWithHint) "Hint text" else null

    DsToggleImpl(
        checked = checked,
        onCheckedChange = {},
        label = labelText,
        size = size,
        enabled = enabled,
        hint = hintText,
        interactionSource = remember { MutableInteractionSource() }
    )
}
