package com.safeNest.demo.features.commonAndroid

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable

// ─────────────────────────────────────────────────────────────────────────────
// 1. Single runtime permission
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun rememberRuntimePermissionLauncher(
    onResult: (granted: Boolean) -> Unit,
) = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission(),
    onResult = onResult,
)

// ─────────────────────────────────────────────────────────────────────────────
// 2. Multiple runtime permissions
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun rememberMultiplePermissionsLauncher(
    onResult: (results: Map<String, Boolean>) -> Unit,
) = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestMultiplePermissions(),
    onResult = onResult,
)

