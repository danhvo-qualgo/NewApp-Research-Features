package net.qualgo.safeNest.onboarding.impl.permission.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.safeNest.demo.features.designSystem.theme.DSRadius
import com.safeNest.demo.features.designSystem.theme.DSSpacing
import com.safeNest.demo.features.designSystem.theme.DSTypography
import com.safeNest.demo.features.designSystem.theme.color.DSColors

// ─────────────────────────────────────────────────────────────────────────────
// Data model
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Holds all data for a single permission row card.
 *
 * @param icon                  Vector icon drawn inside the indigo tile.
 * @param title                 Bold 16sp permission name.
 * @param description           12sp subtitle; shown below the title.
 * @param isGranted             Drives the toggle ON/OFF state.
 * @param isSubscriptionRequired Disables the toggle for premium-only items.
 * @param onToggle              Callback when the switch is flipped.
 */
data class PermissionItemData(
    val iconRes: Int,
    val titleRes: Int,
    val descriptionRes: Int,
    val isGranted: Boolean,
    val isSubscriptionRequired: Boolean = false,
    val onToggle: (Boolean) -> Unit,
)

// ─────────────────────────────────────────────────────────────────────────────
// Reusable composable
// ─────────────────────────────────────────────────────────────────────────────

/**
 * White card row:  [48dp icon tile] [16dp gap] [title + desc (weight 1)]
 *                  [16dp gap] [44×24dp toggle]
 * All padded 16dp inside, corner radius 16dp (DSRadius.xLarge).
 */
@Composable
internal fun PermissionItem(
    data: PermissionItemData,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(DSRadius.xLarge),
        colors = CardDefaults.cardColors(containerColor = DSColors.surfacePrimary),
        elevation = CardDefaults.cardElevation(defaultElevation = DSSpacing.none),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(DSSpacing.s4),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // ── 48 × 48 icon tile ────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .size(DSSpacing.s9)
                    .background(
                        color = DSColors.surfaceActive,
                        shape = RoundedCornerShape(DSRadius.medium),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(data.iconRes),
                    contentDescription = null,
                    tint = DSColors.iconAction,
                    modifier = Modifier.size(DSSpacing.s6),
                )
            }

            Spacer(modifier = Modifier.width(DSSpacing.s4))

            // ── Title + description ──────────────────────────────────────────
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(data.titleRes),
                    style = DSTypography.body2.bold,
                    color = DSColors.textHeading,
                )
                Text(
                    text = stringResource(data.descriptionRes),
                    style = DSTypography.caption2.regular,
                    color = DSColors.textNeutral,
                    modifier = Modifier.padding(top = DSSpacing.s1),
                )
            }

            Spacer(modifier = Modifier.width(DSSpacing.s4))

            // ── Toggle (44 × 24) ─────────────────────────────────────────────
            Switch(
                checked = data.isGranted,
                onCheckedChange = data.onToggle,
                colors = SwitchDefaults.colors(
                    // ON state
                    checkedThumbColor = DSColors.textOnAction,
                    checkedTrackColor = DSColors.surfaceAction,
                    checkedBorderColor = Color.Transparent,
                    // OFF state — rgba(0,0,0,0.10) track per Figma
                    uncheckedThumbColor = DSColors.textOnAction,
                    uncheckedTrackColor = DSColors.surfaceDisabled,
                    uncheckedBorderColor = Color.Transparent,
                    // Disabled (subscription-locked) OFF
                    disabledUncheckedThumbColor = DSColors.textOnAction,
                    disabledUncheckedTrackColor = Color(0x1A000000),
                    disabledUncheckedBorderColor = Color.Transparent,
                ),
            )
        }
    }
}
