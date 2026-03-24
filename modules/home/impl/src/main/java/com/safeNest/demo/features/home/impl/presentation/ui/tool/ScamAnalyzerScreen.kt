package com.safeNest.demo.features.home.impl.presentation.ui.tool

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.safeNest.demo.features.designSystem.component.DSButton
import com.safeNest.demo.features.designSystem.theme.DSSpacing
import com.safeNest.demo.features.designSystem.theme.DSTypography
import com.safeNest.demo.features.designSystem.theme.color.DSColors
import com.safeNest.demo.features.home.impl.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

val PrimaryIndigo = Color(0xFF4F46E5)
val MediaTextColor = Color(0xFF454955)

@Composable
fun ScamAnalyzerScreen(
    onScamAnalyzerClick: () -> Unit,
    onRecordAudioClick: () -> Unit = {},
    onUploadAudioClick: (Uri) -> Unit = {},
    onUploadImageClick: (Uri) -> Unit = {},
    scamAnalyzerViewModel: ScamAnalyzerViewModel = hiltViewModel(),
    sharedText: String? = null,
    onSharedTextConsumed: () -> Unit = {}
) {
    var inputText by remember(sharedText) { mutableStateOf(sharedText ?: "") }
    val uiState by scamAnalyzerViewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(sharedText) {
        Log.d("ScamAnalyzerScreen", "LaunchedEffect triggered with sharedText: $sharedText")
        if (!sharedText.isNullOrBlank()) {
            Log.d(
                "ScamAnalyzerScreen",
                "Auto-filling text and analyzing: ${sharedText.take(50)}..."
            )
            inputText = sharedText
            scamAnalyzerViewModel.analyzeText(sharedText)
            onSharedTextConsumed()
        }
    }

    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { contentUri ->
            // Copy to cache and convert to file:// URI before passing to callback
            coroutineScope.launch(Dispatchers.IO) {
                try {
                    val localFileUri = copyUriToCache(context, contentUri, "audio")
                    if (localFileUri != null) {
                        launch(Dispatchers.Main) {
                            onUploadAudioClick(localFileUri)
                        }
                    } else {
                        Log.e("ScamAnalyzerScreen", "Failed to copy audio file to cache")
                    }
                } catch (e: Exception) {
                    Log.e("ScamAnalyzerScreen", "Error copying audio file", e)
                }
            }
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { contentUri ->
            // Copy to cache and convert to file:// URI before passing to callback
            coroutineScope.launch(Dispatchers.IO) {
                try {
                    val localFileUri = copyUriToCache(context, contentUri, "image")
                    if (localFileUri != null) {
                        launch(Dispatchers.Main) {
                            onUploadImageClick(localFileUri)
                        }
                    } else {
                        Log.e("ScamAnalyzerScreen", "Failed to copy image file to cache")
                    }
                } catch (e: Exception) {
                    Log.e("ScamAnalyzerScreen", "Error copying image file", e)
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        scamAnalyzerViewModel.events.collect { event ->
            when (event) {
                is ScamAnalyzerEvent.AnalysisSuccess -> {
                    onScamAnalyzerClick()
                }
            }
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
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
                    TextInputArea(
                        text = inputText,
                        onTextChange = { inputText = it }
                    )
                    MediaActionsRow(
                        onRecordAudioClick = onRecordAudioClick,
                        onUploadAudioClick = {
                            audioPickerLauncher.launch(arrayOf("audio/*"))
                        },
                        onAttachImageClick = {
                            imagePickerLauncher.launch(arrayOf("image/*"))
                        }
                    )
                    AnalyzeButton(
                        onClick = {
                            scamAnalyzerViewModel.analyzeText(inputText)
                        }
                    )
                }
            }
        }

        if (uiState.isLoading) {
            FullScreenLoading()
        }

        uiState.errorMessage?.let { error ->
            ErrorDialog(
                message = error,
                onDismiss = { scamAnalyzerViewModel.clearError() }
            )
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
private fun TextInputArea(
    text: String,
    onTextChange: (String) -> Unit
) {
    val clipboardManager = LocalClipboardManager.current

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
                .padding(
                    top = DSSpacing.s4,
                    start = DSSpacing.s4,
                    end = DSSpacing.s4,
                    bottom = DSSpacing.s3
                )
        ) {
            BasicTextField(
                value = text,
                onValueChange = onTextChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                textStyle = TextStyle(
                    fontSize = 14.sp,
                    color = DSColors.textBody,
                    lineHeight = 20.sp
                ),
                cursorBrush = SolidColor(DSColors.textActionActive),
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        if (text.isEmpty()) {
                            Text(
                                text = "Message, link, or text here to analyze",
                                style = DSTypography.body2.medium,
                                color = DSColors.textNeutral
                            )
                        }
                        innerTextField()
                    }
                }
            )

            Surface(
                modifier = Modifier.clickable {
                    clipboardManager.getText()?.let { annotatedString ->
                        onTextChange(annotatedString.text)
                    }
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
private fun MediaActionsRow(
    onRecordAudioClick: () -> Unit,
    onUploadAudioClick: () -> Unit,
    onAttachImageClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(DSSpacing.s2)
    ) {
        MediaActionButton(
            modifier = Modifier.weight(1f),
            icon = ImageVector.vectorResource(id = R.drawable.ic_attach_image),
            text = "Attach\nimage",
            onClick = onAttachImageClick
        )
        MediaActionButton(
            modifier = Modifier.weight(1f),
            icon = ImageVector.vectorResource(id = R.drawable.ic_record_audio),
            text = "Record\nAudio",
            onClick = onRecordAudioClick
        )
        MediaActionButton(
            modifier = Modifier.weight(1f),
            icon = ImageVector.vectorResource(id = R.drawable.ic_upload_audio),
            text = "Upload\nAudio",
            onClick = onUploadAudioClick
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
    DSButton(
        text = "Analyze Now",
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        textStyle = DSTypography.caption1.bold,
    )
}

@Composable
private fun FullScreenLoading() {
    Dialog(
        onDismissRequest = { },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = DSColors.surfacePrimary,
                modifier = Modifier.padding(DSSpacing.s6)
            ) {
                Column(
                    modifier = Modifier.padding(DSSpacing.s8),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(DSSpacing.s4)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp),
                        color = DSColors.textActionActive,
                        strokeWidth = 4.dp
                    )
                    Text(
                        text = "Analyzing...",
                        style = DSTypography.body1.semiBold,
                        color = DSColors.textHeading
                    )
                }
            }
        }
    }
}

@Composable
private fun ErrorDialog(
    message: String,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = DSColors.surfacePrimary,
            modifier = Modifier.padding(DSSpacing.s6)
        ) {
            Column(
                modifier = Modifier.padding(DSSpacing.s6),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(DSSpacing.s4)
            ) {
                Text(
                    text = "Error",
                    style = DSTypography.h4.bold,
                    color = DSColors.textError
                )
                Text(
                    text = message,
                    style = DSTypography.body2.medium,
                    color = DSColors.textBody,
                    textAlign = TextAlign.Center
                )
                DSButton(
                    text = "OK",
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = DSTypography.body2.bold
                )
            }
        }
    }
}

/**
 * Copy content:// URI to app's cache directory and return file:// URI
 */
private suspend fun copyUriToCache(
    context: android.content.Context,
    contentUri: Uri,
    type: String
): Uri? = withContext(kotlinx.coroutines.Dispatchers.IO) {
    try {
        val cacheDir = java.io.File(context.cacheDir, "temp_media")
        if (!cacheDir.exists()) {
            cacheDir.mkdirs()
        }

        // Get file name from ContentResolver
        var fileName = "file_${System.currentTimeMillis()}"
        try {
            context.contentResolver.query(
                contentUri,
                arrayOf(android.provider.OpenableColumns.DISPLAY_NAME),
                null,
                null,
                null
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIndex =
                        cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    if (nameIndex >= 0) {
                        val name = cursor.getString(nameIndex)
                        if (!name.isNullOrEmpty()) {
                            fileName = name
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.w("ScamAnalyzerScreen", "Could not get filename: $e")
            // Generate name with correct extension
            val mimeType = context.contentResolver.getType(contentUri)
            val extension = when {
                type == "audio" || mimeType?.startsWith("audio/") == true -> ".mp3"
                type == "image" || mimeType?.startsWith("image/") == true -> {
                    when {
                        mimeType?.contains("png") == true -> ".png"
                        mimeType?.contains("jpeg") == true || mimeType?.contains("jpg") == true -> ".jpg"
                        else -> ".jpg"
                    }
                }

                else -> ""
            }
            fileName = "${type}_${System.currentTimeMillis()}$extension"
        }

        val destinationFile = java.io.File(cacheDir, fileName)

        // Copy file content
        context.contentResolver.openInputStream(contentUri)?.use { input ->
            destinationFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }

        Log.d(
            "ScamAnalyzerScreen",
            "File copied: ${destinationFile.absolutePath}, size: ${destinationFile.length()}"
        )
        Uri.fromFile(destinationFile)
    } catch (e: Exception) {
        Log.e("ScamAnalyzerScreen", "Error copying file to cache", e)
        null
    }
}