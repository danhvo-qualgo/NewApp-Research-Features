package com.safeNest.demo.features.splash.impl.presentation.screen.permissions

import android.content.ClipData
import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.safeNest.demo.features.commonAndroid.rememberMultiplePermissionsLauncher
import com.safeNest.demo.features.commonAndroid.rememberRuntimePermissionLauncher
import com.safeNest.demo.features.designSystem.component.gradientBackground
import com.safeNest.demo.features.designSystem.theme.DSRadius
import com.safeNest.demo.features.designSystem.theme.DSSpacing
import com.safeNest.demo.features.designSystem.theme.DSTypography
import com.safeNest.demo.features.designSystem.theme.color.DSColors
import com.safeNest.demo.features.splash.impl.R
import com.safeNest.demo.features.splash.impl.domain.model.PermissionRequestType
import com.safeNest.demo.features.splash.impl.domain.model.PermissionType
import com.safeNest.demo.features.splash.impl.presentation.screen.permissions.ui.PermissionItem
import com.safeNest.demo.features.splash.impl.presentation.screen.permissions.ui.PermissionItemData
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.IOException
import java.io.InputStream

// ─────────────────────────────────────────────────────────────────────────────
// Screen
// ─────────────────────────────────────────────────────────────────────────────

@RequiresApi(Build.VERSION_CODES.Q)
@Composable
internal fun PermissionsScreen(
    onStartClick: () -> Unit = {},
    viewModel: PermissionViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var permissionTypeRequesting by remember { mutableStateOf<PermissionType?>(null) }
    val launcherRequestPermission = rememberRuntimePermissionLauncher { granted ->
        permissionTypeRequesting?.let { permissionType ->
            viewModel.onAction(
                PermissionAction.UpdatePermissionGrantedState(
                    permissionType,
                    granted
                )
            )
        }
    }

    val launcherRequestPermissions = rememberMultiplePermissionsLauncher { permissionsResult ->
        permissionTypeRequesting?.let { permissionType ->
            val granted = permissionsResult.all { it.value }
            viewModel.onAction(
                PermissionAction.UpdatePermissionGrantedState(
                    permissionType,
                    granted
                )
            )
        }
    }

    // Role permissions must use startActivityForResult — plain startActivity is silently ignored.
    val launcherRequestRole = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        permissionTypeRequesting?.let { permissionType ->
            val granted = result.resultCode == android.app.Activity.RESULT_OK
            viewModel.onAction(
                PermissionAction.UpdatePermissionGrantedState(permissionType, granted)
            )
        }
    }


    // Refresh permission states every time the screen comes back to foreground
    // (the user may have granted a permission in the system settings).
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            viewModel.refreshPermissionStates()
        }
    }

    val context = LocalContext.current
    LaunchedEffect(lifecycleOwner, viewModel.downloadEvent) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            viewModel.downloadEvent.collectLatest { inputStream ->
                saveToDownloads(
                    context,
                    inputStream,
                    "KinShield_ca.pem",
                    "application/x-pem-file"
                )
                viewModel.onSavedComplete()
                Toast.makeText(context, "Cert download complete", Toast.LENGTH_SHORT).show()
            }
        }
    }

    LaunchedEffect(viewModel.event, lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.event.collect { event ->
                when (event) {
                    is PermissionEvent.RequestPermissionEvent -> {
                        permissionTypeRequesting = event.permissionType
                        when (event.permissionType.requestType) {
                            is PermissionRequestType.RunTime -> {
                                launcherRequestPermission.launch(event.permissionType.requestType.permission)
                            }
                            is PermissionRequestType.RunTimes -> {
                                launcherRequestPermissions.launch(event.permissionType.requestType.permissions.toTypedArray())
                            }
                            else -> {}
                        }
                    }
                    is PermissionEvent.RequestRoleEvent -> {
                        permissionTypeRequesting = event.permissionType
                        launcherRequestRole.launch(event.intent)
                    }
                }

            }
        }

    }

    // ── Scrollable content ───────────────────────────────────────────

    PermissionContent(
        onStartClick = onStartClick,
        uiState = uiState,
        onDismiss = viewModel::closeDownloadDialog,
        onAction = viewModel::onAction
    )


}

@Composable
fun PermissionContent(
    onStartClick: () -> Unit,
    onDismiss: () -> Unit,
    uiState: PermissionUiState,
    onAction: (PermissionAction) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize()
            .background(gradientBackground)
            .padding(horizontal = DSSpacing.s6)
            .padding(top = DSSpacing.s6)
            .navigationBarsPadding(),
        verticalArrangement = Arrangement.Top,
    ) {
        Spacer(Modifier.statusBarsPadding())
        Spacer(modifier = Modifier.height(DSSpacing.s6))
        PermissionsHeader()
        Spacer(modifier = Modifier.height(DSSpacing.s8))

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            overscrollEffect = null,
            verticalArrangement = Arrangement.spacedBy(DSSpacing.s2),
        ) {
            // Permission rows
            items(PermissionType.entries) { type ->
                PermissionItem(
                    data = PermissionItemData(
                        iconRes = type.iconRes,
                        titleRes = type.nameRes,
                        descriptionRes = type.descriptionRes,
                        isGranted = uiState.permissionStates[type] == true,
                        onToggle = {
                            onAction(PermissionAction.TogglePermission(type))
                        },
                    ),
                )
            }

            item {
                PermissionItem(
                    data = PermissionItemData(
                        iconRes =  R.drawable.ic_server,
                        titleRes = R.string.permission_private_dns_title,
                        descriptionRes = R.string.permission_private_dns_desc,
                        isGranted = uiState.dnsPermissionState,
                        onToggle = {
                            onAction(PermissionAction.ToggleDsnPermission)
                        }
                    )
                )
            }

            item {
                Spacer(modifier = Modifier.height(DSSpacing.s2))
            }
            item {
                Button(
                    onClick = onStartClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(DSRadius.round),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DSColors.current.primary,
                        contentColor = DSColors.textOnAction,
                        disabledContainerColor = DSColors.current.primaryLighter
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = DSSpacing.none),
                    enabled = uiState.allPermissionsGranted
                ) {
                    Text(
                        text = stringResource(R.string.permission_start_button),
                        style = DSTypography.body2.bold,
                    )
                }
            }
        }

    }
    val clipboardManager = LocalClipboard.current
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    if(uiState.showDownloadDialog) {
        DownloadCertDialog(
            onDownloadClick = {
                onAction(PermissionAction.ClickDownloadCa)
            },
            onCopyClick = {
                coroutineScope.launch {
                    clipboardManager.setClipEntry(ClipEntry(ClipData.newPlainText("dsn_link", "demo-kinshield-dns.qualgo.dev")))
                    Toast.makeText(context, "Copy success", Toast.LENGTH_SHORT).show()
                }
            },
            onDismiss = onDismiss
        )
    }


    FullScreenLoading(uiState.loading)
}

@RequiresApi(Build.VERSION_CODES.Q)
private fun saveToDownloads(
    context: Context,
    inputStream: InputStream,
    fileName: String,
    mimeType: String
) {
    val resolver = context.contentResolver

    val contentValues = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
        put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
        put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
        put(MediaStore.MediaColumns.IS_PENDING, 1)
    }

    val uri = resolver.insert(
        MediaStore.Downloads.EXTERNAL_CONTENT_URI,
        contentValues
    ) ?: throw IOException("Failed to create MediaStore record")

    resolver.openOutputStream(uri)?.use { outputStream ->
        inputStream.use { input ->
            input.copyTo(outputStream)
        }
    }

    // Mark file as finished
    contentValues.clear()
    contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
    resolver.update(uri, contentValues, null, null)
}


// ─────────────────────────────────────────────────────────────────────────────
// Header
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun PermissionsHeader() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(DSSpacing.s6),
    ) {
        // White circle + shield icon (64 × 64 per Figma)
        Box(
            modifier = Modifier
                .size(DSSpacing.s10)
                .background(
                    color = DSColors.surfacePrimary,
                    shape = CircleShape,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_shield),
                contentDescription = null,
                tint = DSColors.iconAction,
                modifier = Modifier.size(DSSpacing.s8 /* 32dp */),
            )
        }

        // Title + subtitle block (4dp gap between them)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(DSSpacing.s1),
        ) {
            Text(
                text = stringResource(R.string.permissions_screen_title),
                style = DSTypography.h3.bold,
                color = DSColors.textActionActive,
                textAlign = TextAlign.Center,
            )
            Text(
                text = stringResource(R.string.permissions_screen_subtitle),
                style = DSTypography.body2.regular,
                color = DSColors.textAction,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
fun DownloadCertDialog(
    onDownloadClick: () -> Unit,
    onCopyClick: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Download cert")
        },
        text = {
            Text(
                text = "After install cert copy DNS provider then using in PrivateDNS setting"
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onDownloadClick()
                    onDismiss()
                }
            ) {
                Text("Download")
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onCopyClick()
                }
            ) {
                Text("Copy")
            }
        },
        titleContentColor = DSColors.textAction,
    )
}

@Composable
fun FullScreenLoading(
    isLoading: Boolean
) {
    if (isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.4f))
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) {} // block click
        ) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Previews
// ─────────────────────────────────────────────────────────────────────────────

@RequiresApi(Build.VERSION_CODES.Q)
@Preview(
    name = "PermissionsScreen – Pixel 5",
    device = Devices.PIXEL_5,
    showSystemUi = true,
)
@Composable
private fun PreviewPermissionsScreenPixel5() {
    PermissionsScreen()
}

@RequiresApi(Build.VERSION_CODES.Q)
@Preview(
    name = "PermissionsScreen – Compact",
    widthDp = 360,
    heightDp = 800,
    showBackground = true,
)
@Composable
private fun PreviewPermissionsScreenCompact() {
    PermissionsScreen()
}
