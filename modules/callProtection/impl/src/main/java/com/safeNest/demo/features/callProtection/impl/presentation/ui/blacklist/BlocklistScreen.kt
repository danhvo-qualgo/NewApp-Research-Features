package com.safeNest.demo.features.callProtection.impl.presentation.ui.blacklist

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.CallLog
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.safeNest.demo.features.callProtection.impl.R
import com.safeNest.demo.features.callProtection.impl.domain.model.BlacklistPattern
import com.safeNest.demo.features.designSystem.component.DsToggle
import com.safeNest.demo.features.designSystem.theme.DSSpacing
import com.safeNest.demo.features.designSystem.theme.DSTypography
import com.safeNest.demo.features.designSystem.theme.color.DSColors

@Composable
fun BlocklistScreen(
    viewModel: BlocklistViewModel = hiltViewModel(),
    onAddClick: () -> Unit = {},
) {
    val patterns by viewModel.blacklist.collectAsState()
    val isEnable by viewModel.isEnable.collectAsState()
    val context = LocalContext.current
    val activity = context as? Activity
    var input by remember { mutableStateOf("") }
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
                            Icon(ImageVector.vectorResource(R.drawable.ic_browsing), contentDescription = "Globe", tint = Color.Unspecified)
                        }
                        Column(modifier = Modifier.weight(1f).padding(horizontal = DSSpacing.s4)) {
                            Text("Blocklist Protection", style = DSTypography.body2.bold, color = DSColors.textHeading)
                            Spacer(modifier = Modifier.height(DSSpacing.half))
                            Text("Automatically block calls from unknown patterns", style = DSTypography.caption1.regular, color = DSColors.textNeutral)
                        }
                        DsToggle(
                            checked = isEnable,
                            onCheckedChange = {
                                viewModel.enable(it)
                            },
                        )
                    }
                }
                if (isEnable) {
                    Spacer(modifier = Modifier.height(DSSpacing.s6))
                    Text(
                        "Blocked Patterns (${patterns.size} active)",
                        color = DSColors.textBody,
                        style = DSTypography.caption1.semiBold
                    )
                }
                Spacer(modifier = Modifier.height(DSSpacing.s4))
            }
            if (isEnable) {
                items(patterns) {
                    BlockedPatternItem(
                        BlacklistPattern(
                            it.pattern, it.description
                        )
                    ) {
                        viewModel.remove(it.pattern)
                    }
                    Spacer(modifier = Modifier.height(DSSpacing.s2))
                }
            }

            item {
                Spacer(modifier = Modifier.height(DSSpacing.s4))

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DSColors.surfaceActive),
                    border = BorderStroke(1.dp, DSColors.surfaceActionDisabled),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(DSSpacing.s5)) {
                        Icon(ImageVector.vectorResource(R.drawable.ic_image_gallery), tint = Color.Unspecified, contentDescription = "Info")
                        Spacer(modifier = Modifier.width(DSSpacing.s3))

                        val text = buildAnnotatedString {
                            append("Incoming calls matching these patterns will be automatically silenced and logged as blocked. You can view blocked calls in your ")
                            withLink(
                                LinkAnnotation.Clickable(
                                    tag = "CALL_HISTORY",
                                    styles = TextLinkStyles(
                                        style = DSTypography.caption2.medium
                                            .copy(
                                                color = DSColors.textAction,
                                                textDecoration = TextDecoration.Underline
                                            )
                                            .toSpanStyle()
                                    )
                                ) {
                                    openSystemCallHistory(context)
                                }
                            ) {
                                append("Call History")
                            }
                            append(".")
                        }
                        Text(
                            text = text,
                            color = DSColors.textBody,
                            style = DSTypography.caption1.regular,
                            lineHeight = 20.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(DSSpacing.s6))
            }
        }
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

fun openSystemCallHistory(context: Context) {
    val intent = Intent(Intent.ACTION_VIEW).apply {
        data = CallLog.Calls.CONTENT_URI
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }

    try {
        context.startActivity(intent)
    } catch (e: Exception) {
        try {
            val fallbackIntent = Intent(Intent.ACTION_DIAL).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(fallbackIntent)
        } catch (e2: Exception) {
            Toast.makeText(context, "Cannot find any app to open call history", Toast.LENGTH_SHORT).show()
        }
    }
}

@Composable
private fun BlockedPatternItem(
    pattern: BlacklistPattern,
    onDeleteClick: () -> Unit = {}
) {
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
                modifier = Modifier.size(32.dp).background(DSColors.surfaceErrorLightest, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(ImageVector.vectorResource(R.drawable.ic_block_red), contentDescription = "Block", tint = Color.Unspecified, modifier = Modifier.size(16.dp))
            }
            Column(modifier = Modifier.weight(1f).padding(horizontal = DSSpacing.s4)) {
                Text(pattern.pattern, color = DSColors.textHeading, style = DSTypography.body2.bold)
                Spacer(modifier = Modifier.height(DSSpacing.half))
                Text(pattern.description, color = DSColors.textNeutral, style = DSTypography.caption2.regular)
            }
            Icon(ImageVector.vectorResource(R.drawable.ic_delete_trash), contentDescription = "Delete", tint = Color.Unspecified, modifier = Modifier.clickable {
                onDeleteClick()
            })
        }
    }
}