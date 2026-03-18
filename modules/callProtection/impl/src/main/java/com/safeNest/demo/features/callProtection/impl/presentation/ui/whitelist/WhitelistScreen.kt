package com.safeNest.demo.features.callProtection.impl.presentation.ui.whitelist

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.safeNest.demo.features.callProtection.impl.R
import com.safeNest.demo.features.callProtection.impl.domain.common.formatBeautifulNumber
import com.safeNest.demo.features.callProtection.impl.domain.model.PhoneNumberInfo
import com.safeNest.demo.features.designSystem.component.DsToggle
import com.safeNest.demo.features.designSystem.theme.DSSpacing
import com.safeNest.demo.features.designSystem.theme.DSTypography
import com.safeNest.demo.features.designSystem.theme.color.DSColors

@Composable
fun WhitelistScreen(
    viewModel: WhitelistViewModel = hiltViewModel(),
    paddingValues: PaddingValues,
    onAddClick: () -> Unit = {}
) {
    val allowedContacts by viewModel.whitelist.collectAsState()
    val isEnable by viewModel.isEnable.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = DSSpacing.s6)
    ) {
        LazyColumn(modifier = Modifier
            .fillMaxWidth().weight(1f)) {
            item {
                Spacer(modifier = Modifier.height(DSSpacing.s6))
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DSColors.surface1),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(DSSpacing.s4),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier.size(48.dp).background(DSColors.surfaceActive, RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(ImageVector.vectorResource(R.drawable.ic_call_incoming), contentDescription = "Call", tint = Color.Unspecified)
                        }
                        Column(modifier = Modifier.weight(1f).padding(horizontal = DSSpacing.s4)) {
                            Text("Whitelist Protection", style = DSTypography.body2.bold, color = DSColors.textHeading)
                            Spacer(modifier = Modifier.height(DSSpacing.half))
                            Text("Only allowed contacts can reach you", style = DSTypography.caption1.regular, color = DSColors.textNeutral)
                        }
                        DsToggle(
                            checked = isEnable,
                            onCheckedChange = {
                                viewModel.enable(it)
                            },
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(DSSpacing.s6))
                if (isEnable)
                    Text("Allowed Contacts", color = DSColors.textBody, style = DSTypography.caption1.semiBold)
                Spacer(modifier = Modifier.height(DSSpacing.s4))

            }
            if (allowedContacts.isNotEmpty() && isEnable)
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = DSColors.surface1)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(DSSpacing.s6),
                            verticalArrangement = Arrangement.spacedBy(DSSpacing.s6)
                        ) {
                            allowedContacts.forEachIndexed { index, contact ->
                                ContactItem(contact, index != allowedContacts.lastIndex) {
                                    viewModel.remove(contact.phoneNumber)
                                }
                            }
                        }
                    }
                }
        }
        Spacer(modifier = Modifier.height(DSSpacing.s6))
        Button(
            onClick = { onAddClick() },
            modifier = Modifier
                .fillMaxWidth().padding(vertical = DSSpacing.s4)
                .navigationBarsPadding(),
            contentPadding = PaddingValues(DSSpacing.s4),
            colors = ButtonDefaults.buttonColors(
                containerColor = DSColors.surfaceAction
            ),
            shape = RoundedCornerShape(32.dp)
        ) {
            Icon(ImageVector.vectorResource(R.drawable.ic_plus), contentDescription = "Add")
            Spacer(modifier = Modifier.width(12.dp))
            Text("Add", style = DSTypography.body2.bold, color = DSColors.textOnAction)
        }
    }
}


@Composable
fun ContactItem(phoneNumberInfo: PhoneNumberInfo, isShowDivider: Boolean, onDelete: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(40.dp).background(DSColors.surfaceActive, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(ImageVector.vectorResource(R.drawable.ic_user_profile), contentDescription = "Person", tint = Color.Unspecified, modifier = Modifier.size(16.dp))
            }
            Column(modifier = Modifier.weight(1f).padding(horizontal = DSSpacing.s4)) {
                Text(phoneNumberInfo.name, color = DSColors.textHeading, style = DSTypography.body2.bold)
                Spacer(modifier = Modifier.height(DSSpacing.half))
                Text(formatBeautifulNumber(phoneNumberInfo.phoneNumber), color = DSColors.textNeutral, style = DSTypography.caption2.regular)
            }
            Icon(ImageVector.vectorResource(R.drawable.ic_delete_trash), contentDescription = "Delete", tint = Color.Unspecified, modifier = Modifier.clickable {
                onDelete()
            })
        }
        if (isShowDivider) {
            Spacer(modifier = Modifier.height(DSSpacing.s6))
            Spacer(modifier = Modifier.height(1.dp).fillMaxWidth().background(DSColors.borderPrimary))
        }
    }
}