package com.safeNest.demo.features.designSystem.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.safeNest.demo.features.designSystem.R
import com.safeNest.demo.features.designSystem.theme.DSRadius
import com.safeNest.demo.features.designSystem.theme.DSSpacing
import com.safeNest.demo.features.designSystem.theme.DSTypography
import com.safeNest.demo.features.designSystem.theme.color.DSColors

@Composable
fun <T> DSDropdown(
    selectedValue: T,
    options: List<T>,
    onValueChange: (T) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    getDisplayText: (T) -> String = { it.toString() }
) {
    var expanded by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        label = "dropdown_rotation"
    )

    Box(modifier = modifier) {
        Card(
            shape = RoundedCornerShape(DSRadius.large),
            colors = CardDefaults.cardColors(containerColor = DSColors.surface1),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(DSRadius.large))
                .clickable { expanded = true }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = DSSpacing.s4, vertical = DSSpacing.s3),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    label?.let {
                        Text(
                            text = it,
                            style = DSTypography.caption2.regular,
                            color = DSColors.textBody.copy(alpha = 0.7f)
                        )
                    }
                    Text(
                        text = getDisplayText(selectedValue),
                        style = DSTypography.body2.semiBold,
                        color = DSColors.textBody,
                        modifier = Modifier.padding(top = if (label != null) 4.dp else 0.dp)
                    )
                }
                
                Icon(
                    painter = painterResource(R.drawable.ic_chevron_down),
                    contentDescription = "Dropdown",
                    tint = DSColors.iconBody,
                    modifier = Modifier.rotate(rotation)
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .fillMaxWidth()
                .background(DSColors.surface1)
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = getDisplayText(option),
                            style = DSTypography.body2.medium,
                            color = if (option == selectedValue) {
                                DSColors.textAction
                            } else {
                                DSColors.textBody
                            }
                        )
                    },
                    onClick = {
                        onValueChange(option)
                        expanded = false
                    }
                )
            }
        }
    }
}
