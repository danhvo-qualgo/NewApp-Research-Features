package com.safeNest.demo.features.home.impl.presentation.ui.tool

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.outlined.FileUpload
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.safeNest.demo.features.designSystem.component.DSButton
import com.safeNest.demo.features.designSystem.theme.DSSpacing
import com.safeNest.demo.features.designSystem.theme.DSTypography
import com.safeNest.demo.features.designSystem.theme.color.DSColors
import com.safeNest.demo.features.home.impl.R
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

val PrimaryIndigo = Color(0xFF4F46E5)
val MediaTextColor = Color(0xFF454955)

@Composable
fun ScamAnalyzerScreen(
    onScamAnalyzerClick: () -> Unit,
    scamAnalyzerViewModel: ScamAnalyzerViewModel = hiltViewModel()
) {
    // Scaffold provides the structural layout for the top content and bottom dockbar
    Scaffold(
        containerColor = Color.Transparent, // Let the background box show through
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(top = DSSpacing.s9)
            ) {
                TopHeader()

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = DSSpacing.s6),
                    verticalArrangement = Arrangement.spacedBy(DSSpacing.s6)
                ) {
                    TextInputArea()
                    MediaActionsRow()
                    AnalyzeButton {
                        onScamAnalyzerClick()
                        scamAnalyzerViewModel.analyzeText("Text")
                    }
                }
            }
        }
    }
}

@Composable
private fun TopHeader() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 64.dp, bottom = 24.dp, start = 24.dp, end = 24.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Scam Analyzer",
            style = DSTypography.h2.bold,
            color = DSColors.textActionActive,
            lineHeight = 42.sp
        )
        Text(
            text = "Identify scams in messages, links, or images using our AI-powered analyzer.",
            style = DSTypography.body2.medium,
            color = DSColors.textHeading,
            lineHeight = 24.sp
        )
    }
}

@Composable
private fun TextInputArea() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DSColors.surface1),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = DSSpacing.s4, start = DSSpacing.s4, end = DSSpacing.s4, bottom = DSSpacing.s3)
        ) {
            Text(
                text = "Message, link, or text here to analyze",
                style = DSTypography.body2.medium,
                color = DSColors.textNeutral,
                modifier = Modifier.weight(1f)
            )

            Surface(
                modifier = Modifier.clickable {

                },
                shape = CircleShape,
                color = DSColors.surface2,
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = DSSpacing.s3, vertical = DSSpacing.s2),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(DSSpacing.s1)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentPaste,
                        contentDescription = "Paste",
                        tint = DSColors.iconHeading,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "Paste from clipboard",
                        style = DSTypography.caption2.medium,
                        color = DSColors.textHeading
                    )
                }
            }
        }
    }
}

@Composable
private fun MediaActionsRow() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(DSSpacing.s2)
    ) {
        MediaActionButton(
            modifier = Modifier.weight(1f),
            icon = ImageVector.vectorResource(id = R.drawable.ic_attach_image),
            text = "Attach\nimage",
            onClick = { /* Handle image attachment */ }
        )
        MediaActionButton(
            modifier = Modifier.weight(1f),
            icon = ImageVector.vectorResource(id = R.drawable.ic_record_audio),
            text = "Record\nAudio",
            onClick = { /* Handle record */ }
        )
        MediaActionButton(
            modifier = Modifier.weight(1f),
            icon = ImageVector.vectorResource(id = R.drawable.ic_upload_audio),
            text = "Upload\nAudio",
            onClick = { /* Handle audio upload */ }
        )
    }
}

@Composable
private fun MediaActionButton(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    text: String,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(120.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DSColors.surface1)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(DSSpacing.s4),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.size(DSSpacing.s4)
            )
            Spacer(modifier = Modifier.height(DSSpacing.s2))
            Text(
                text = text,
                style = DSTypography.caption2.semiBold,
                color = DSColors.textBody,
                textAlign = TextAlign.Center,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
private fun AnalyzeButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        textStyle = DSTypography.caption1.bold,
    )
}