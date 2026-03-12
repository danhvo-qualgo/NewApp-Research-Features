package net.qualgo.safeNest.permissionmanager.impl.presentation.screen

import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import net.qualgo.safeNest.permissionmanager.impl.R
import net.qualgo.safeNest.permissionmanager.impl.domain.model.AppCategory
import net.qualgo.safeNest.permissionmanager.impl.domain.model.AppInfo
import net.qualgo.safeNest.permissionmanager.impl.domain.model.InstallSource
import net.qualgo.safeNest.permissionmanager.impl.domain.model.PermissionInfo
import net.qualgo.safeNest.permissionmanager.impl.domain.model.PermissionProtectionLevel

// ─────────────────────────────────────────────────────────────────────────────
// Entry point
// ─────────────────────────────────────────────────────────────────────────────

@Composable
internal fun PermissionManagerScreen(
    viewModel: PermissionManagerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    PermissionManagerContent(
        uiState = uiState,
        onSearchQueryChange = viewModel::updateSearchQuery,
        onToggleSystemApps = viewModel::toggleShowSystemApps,
        onRetry = viewModel::loadApps
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Stateless content
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PermissionManagerContent(
    uiState: PermissionManagerUiState,
    onSearchQueryChange: (String) -> Unit,
    onToggleSystemApps: () -> Unit,
    onRetry: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.permission_manager_title),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // ── Search + filter bar ──────────────────────────────────────────
            Surface(shadowElevation = 2.dp) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = uiState.searchQuery,
                        onValueChange = onSearchQueryChange,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text(stringResource(R.string.permission_manager_search_hint)) },
                        singleLine = true,
                        shape = RoundedCornerShape(50)
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = uiState.showSystemApps,
                            onClick = onToggleSystemApps,
                            label = { Text(stringResource(R.string.permission_manager_show_system)) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                        if (!uiState.isLoading) {
                            Text(
                                text = stringResource(
                                    R.string.permission_manager_app_count,
                                    uiState.displayedApps.size
                                ),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // ── Body ─────────────────────────────────────────────────────────
            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    uiState.isLoading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }

                    uiState.errorMessage != null -> {
                        Column(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.permission_manager_error),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.error
                            )
                            Text(
                                text = uiState.errorMessage,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Button(onClick = onRetry) {
                                Text(stringResource(R.string.permission_manager_retry))
                            }
                        }
                    }

                    uiState.displayedApps.isEmpty() -> {
                        Text(
                            text = stringResource(R.string.permission_manager_no_apps),
                            modifier = Modifier.align(Alignment.Center),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    else -> {
                        AppList(apps = uiState.displayedApps)
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// App list
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun AppList(apps: List<AppInfo>) {
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(apps, key = { it.packageName }) { app ->
            AppCard(app = app)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// App card (expandable)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun AppCard(app: AppInfo) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // ── Header row ───────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AppIconImage(packageName = app.packageName)

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = app.appName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = app.packageName,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        InstallSourceBadge(source = app.installSource)
                        CategoryBadge(category = app.category)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    PermissionSummaryChips(app)
                }

                // Expand indicator
                Text(
                    text = if (expanded) "▲" else "▼",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }

            // ── Permission list (animated) ───────────────────────────────────
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                ) {
                    if (app.permissions.isEmpty()) {
                        Text(
                            text = "  No permissions declared",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(12.dp)
                        )
                    } else {
                        app.permissions.forEachIndexed { index, perm ->
                            PermissionRow(permission = perm)
                            if (index < app.permissions.lastIndex) {
                                Divider(
                                    modifier = Modifier.padding(horizontal = 12.dp),
                                    thickness = 0.5.dp,
                                    color = MaterialTheme.colorScheme.outlineVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Permission summary chips
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun PermissionSummaryChips(app: AppInfo) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        MiniChip(
            label = "${app.grantedCount} granted",
            bgColor = GrantedColor.copy(alpha = 0.15f),
            textColor = GrantedColor
        )
        MiniChip(
            label = "${app.deniedCount} denied",
            bgColor = DeniedColor.copy(alpha = 0.15f),
            textColor = DeniedColor
        )
        if (app.dangerousCount > 0) {
            MiniChip(
                label = "${app.dangerousCount} ⚠",
                bgColor = DangerousColor.copy(alpha = 0.15f),
                textColor = DangerousColor
            )
        }
    }
}

@Composable
private fun MiniChip(label: String, bgColor: Color, textColor: Color) {
    Text(
        text = label,
        modifier = Modifier
            .background(bgColor, RoundedCornerShape(50))
            .padding(horizontal = 6.dp, vertical = 2.dp),
        style = MaterialTheme.typography.labelSmall,
        color = textColor,
        fontSize = 10.sp
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Single permission row
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun PermissionRow(permission: PermissionInfo) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Protection level dot
        ProtectionDot(level = permission.protectionLevel)

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = permission.label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = permission.name,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Granted / Denied badge
        GrantedBadge(isGranted = permission.isGranted)
    }
}

@Composable
private fun ProtectionDot(level: PermissionProtectionLevel) {
    val color = when (level) {
        PermissionProtectionLevel.DANGEROUS -> DangerousColor
        PermissionProtectionLevel.NORMAL    -> NormalColor
        PermissionProtectionLevel.SIGNATURE -> SignatureColor
        PermissionProtectionLevel.OTHER     -> OtherColor
    }
    val tooltip = when (level) {
        PermissionProtectionLevel.DANGEROUS -> "D"
        PermissionProtectionLevel.NORMAL    -> "N"
        PermissionProtectionLevel.SIGNATURE -> "S"
        PermissionProtectionLevel.OTHER     -> "?"
    }
    Box(
        modifier = Modifier
            .size(22.dp)
            .background(color.copy(alpha = 0.18f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = tooltip,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp
        )
    }
}

@Composable
private fun GrantedBadge(isGranted: Boolean) {
    val bg = if (isGranted) GrantedColor.copy(alpha = 0.15f) else DeniedColor.copy(alpha = 0.15f)
    val textColor = if (isGranted) GrantedColor else DeniedColor
    val label = if (isGranted) "✓ Granted" else "✗ Denied"
    Text(
        text = label,
        modifier = Modifier
            .background(bg, RoundedCornerShape(50))
            .padding(horizontal = 8.dp, vertical = 3.dp),
        style = MaterialTheme.typography.labelSmall,
        color = textColor,
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// App icon loader
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun AppIconImage(packageName: String) {
    val context = LocalContext.current
    val bitmap: ImageBitmap? = remember(packageName) {
        runCatching {
            val drawable = context.packageManager.getApplicationIcon(packageName)
            drawable.toSafeBitmap().asImageBitmap()
        }.getOrNull()
    }

    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap,
                contentDescription = null,
                modifier = Modifier.size(40.dp)
            )
        } else {
            Text(
                text = packageName.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

/** Safely converts any Drawable to a Bitmap. */
private fun android.graphics.drawable.Drawable.toSafeBitmap(): Bitmap {
    if (this is BitmapDrawable && bitmap != null) return bitmap
    val w = intrinsicWidth.coerceAtLeast(1)
    val h = intrinsicHeight.coerceAtLeast(1)
    val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bmp)
    setBounds(0, 0, canvas.width, canvas.height)
    draw(canvas)
    return bmp
}

// ─────────────────────────────────────────────────────────────────────────────
// Category badge
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun CategoryBadge(category: AppCategory) {
    val bgColor = categoryColor(category).copy(alpha = 0.15f)
    val textColor = categoryColor(category)
    Text(
        text = "${category.emoji} ${category.label}",
        modifier = Modifier
            .background(bgColor, RoundedCornerShape(50))
            .padding(horizontal = 6.dp, vertical = 2.dp),
        style = MaterialTheme.typography.labelSmall,
        color = textColor,
        fontSize = 10.sp,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
private fun categoryColor(category: AppCategory): Color = when (category) {
    AppCategory.GAME         -> Color(0xFF7B1FA2) // purple
    AppCategory.AUDIO        -> Color(0xFF00838F) // cyan
    AppCategory.VIDEO        -> Color(0xFFAD1457) // pink
    AppCategory.IMAGE        -> Color(0xFF558B2F) // light green
    AppCategory.SOCIAL       -> Color(0xFF1565C0) // blue
    AppCategory.NEWS         -> Color(0xFF6D4C41) // brown
    AppCategory.MAPS         -> Color(0xFF2E7D32) // green
    AppCategory.PRODUCTIVITY -> Color(0xFFEF6C00) // orange
    AppCategory.ACCESSIBILITY-> Color(0xFF00695C) // teal
    AppCategory.UNDEFINED    -> Color(0xFF78909C) // grey
}

// ─────────────────────────────────────────────────────────────────────────────
// Install source badge
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun InstallSourceBadge(source: InstallSource) {
    val (icon, label, bgColor, textColor) = when (source) {
        is InstallSource.KnownStore -> InstallSourceStyle(
            icon = "▶",
            label = source.storeName,
            bgColor = StoreColor.copy(alpha = 0.15f),
            textColor = StoreColor
        )
        is InstallSource.Sideloaded -> InstallSourceStyle(
            icon = "📦",
            label = if (source.installerPackage != null)
                "Sideloaded (${source.installerPackage})"
            else
                "Sideloaded",
            bgColor = SideloadColor.copy(alpha = 0.15f),
            textColor = SideloadColor
        )
        InstallSource.Preinstalled -> InstallSourceStyle(
            icon = "⚙",
            label = "Pre-installed",
            bgColor = PreinstalledColor.copy(alpha = 0.15f),
            textColor = PreinstalledColor
        )
        InstallSource.Unknown -> InstallSourceStyle(
            icon = "?",
            label = "Unknown source",
            bgColor = OtherColor.copy(alpha = 0.15f),
            textColor = OtherColor
        )
    }
    Text(
        text = "$icon $label",
        modifier = Modifier
            .background(bgColor, RoundedCornerShape(50))
            .padding(horizontal = 6.dp, vertical = 2.dp),
        style = MaterialTheme.typography.labelSmall,
        color = textColor,
        fontSize = 10.sp,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}

private data class InstallSourceStyle(
    val icon: String,
    val label: String,
    val bgColor: Color,
    val textColor: Color
)

// ─────────────────────────────────────────────────────────────────────────────
// Colour constants
// ─────────────────────────────────────────────────────────────────────────────

private val GrantedColor       = Color(0xFF2E7D32) // green
private val DeniedColor        = Color(0xFFC62828) // red
private val DangerousColor     = Color(0xFFE65100) // deep orange
private val NormalColor        = Color(0xFF546E7A) // blue-grey
private val SignatureColor     = Color(0xFF6A1B9A) // purple
private val OtherColor         = Color(0xFF78909C) // grey
// Install source badge colours
private val StoreColor         = Color(0xFF1565C0) // blue  – known store
private val SideloadColor      = Color(0xFFBF360C) // burnt orange – sideloaded
private val PreinstalledColor  = Color(0xFF37474F) // dark blue-grey – preinstalled
