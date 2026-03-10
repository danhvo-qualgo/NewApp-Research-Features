package net.qualgo.safeNest.features.phishingDetection.impl.presentation

import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.uney.core.router.RouterManager
import com.uney.core.router.compose.LocalRouterManager
import dagger.hilt.android.AndroidEntryPoint
import net.qualgo.safeNest.features.phishingDetection.impl.presentation.models.ExtractedEntities
import net.qualgo.safeNest.features.phishingDetection.impl.presentation.models.WebsiteMetadata
import net.qualgo.safeNest.features.phishingDetection.impl.presentation.ModelState
import net.qualgo.safeNest.features.phishingDetection.impl.presentation.OptionViewModel
import net.qualgo.safeNest.features.phishingDetection.impl.presentation.textPhisingDetection.ExtractionMethod
import net.qualgo.safeNest.features.phishingDetection.impl.presentation.textPhisingDetection.TextImageUiState
import net.qualgo.safeNest.features.phishingDetection.impl.presentation.textPhisingDetection.TextImageViewModel
import net.qualgo.safeNest.features.phishingDetection.impl.presentation.urlChecker.PhishingDetectionViewModel
import net.qualgo.safeNest.features.phishingDetection.impl.presentation.urlChecker.PhishingUiEffect
import net.qualgo.safeNest.features.phishingDetection.impl.presentation.urlChecker.PhishingUiState
import net.qualgo.safeNest.features.phishingDetection.impl.presentation.urlChecker.WebsiteInspectorWebView
import javax.inject.Inject

@AndroidEntryPoint
class PhishingDetectionActivity : ComponentActivity() {
    @Inject
    lateinit var routerManager: RouterManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
        )

        setContent {
            androidx.compose.runtime.CompositionLocalProvider(LocalRouterManager provides routerManager) {
                val navController = rememberNavController()
                NavHost(
                    navController = navController,
                    startDestination = "option"
                ) {
                    composable("option") {
                        PhishingDetectionOptionScreen(
                            onUrlCheckerClick = { navController.navigate("url_checker") },
                            onTextImageCheckerClick = { navController.navigate("text_image_checker") }
                        )
                    }
                    composable("url_checker") {
                        PhishingUrlDetectionScreen()
                    }
                    composable("text_image_checker") {
                        PhishingTextDetectionScreen()
                    }
                }
            }
        }
    }
}

@Composable
fun PhishingDetectionOptionScreen(
    viewModel: OptionViewModel = hiltViewModel(),
    onUrlCheckerClick: () -> Unit = {},
    onTextImageCheckerClick: () -> Unit = {}
) {
    val modelState by viewModel.modelState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = androidx.compose.ui.graphics.Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .systemBarsPadding(),
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Phishing Detection",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ── Model status ─────────────────────────────────────────────────
            when (val state = modelState) {
                is ModelState.Idle -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        Text(
                            text = "Preparing model…",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                is ModelState.Downloading -> {
                    Text(
                        text = "Downloading model… ${state.progressPercent}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { state.progressPercent / 100f },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                is ModelState.Loading -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        Text(
                            text = "Loading model into memory…",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                is ModelState.Ready -> {
                    Text(
                        text = "Model ready",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                is ModelState.Error -> {
                    Text(
                        text = "Model error: ${state.message}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            val modelReady = modelState is ModelState.Ready

            Button(
                onClick = onUrlCheckerClick,
                modifier = Modifier.fillMaxWidth(),
                enabled = modelReady,
            ) {
                Text("URL Checker")
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onTextImageCheckerClick,
                modifier = Modifier.fillMaxWidth(),
                enabled = modelReady,
            ) {
                Text("Text / Image Checker")
            }
        }
    }
}

@Composable
fun PhishingTextDetectionScreen(
    viewModel: TextImageViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var textInput by rememberSaveable { mutableStateOf("") }
    var selectedMethod by rememberSaveable { mutableStateOf(ExtractionMethod.REGEX) }
    val focusManager = LocalFocusManager.current

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            focusManager.clearFocus()
            viewModel.onImageSelected(uri, selectedMethod)
        }
    }

    val isBusy = uiState is TextImageUiState.OcrRunning ||
        uiState is TextImageUiState.Extracting ||
        uiState is TextImageUiState.DeepAnalyzing

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = androidx.compose.ui.graphics.Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .systemBarsPadding()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Text / Image Checker",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ── Extraction method toggle ──────────────────────────────────────
            Text(
                text = "Extraction method",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ExtractionMethod.entries.forEach { method ->
                    FilterChip(
                        selected = selectedMethod == method,
                        onClick = { selectedMethod = method },
                        label = {
                            Text(
                                text = when (method) {
                                    ExtractionMethod.REGEX -> "Regex"
                                    ExtractionMethod.LLM -> "LLM (Qwen3-0.6B)"
                                }
                            )
                        },
                        enabled = !isBusy,
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Text input ────────────────────────────────────────────────────
            OutlinedTextField(
                value = textInput,
                onValueChange = { textInput = it },
                label = { Text("Enter text to analyze") },
                placeholder = { Text("Paste message, email, SMS…") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 4,
                maxLines = 8,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Default,
                ),
                enabled = !isBusy,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Button(
                    onClick = {
                        focusManager.clearFocus()
                        viewModel.onTextSubmit(textInput, selectedMethod)
                    },
                    modifier = Modifier.weight(1f),
                    enabled = !isBusy && textInput.isNotBlank(),
                ) {
                    Text("Extract")
                }

                OutlinedButton(
                    onClick = {
                        focusManager.clearFocus()
                        imagePicker.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    modifier = Modifier.weight(1f),
                    enabled = !isBusy,
                ) {
                    Text("Upload Image")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── State feedback ────────────────────────────────────────────────
            when (val state = uiState) {
                is TextImageUiState.Idle -> Unit

                is TextImageUiState.OcrRunning -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        Text(
                            text = "Reading image…",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                is TextImageUiState.Extracting -> {
                    val label = when (state.method) {
                        ExtractionMethod.REGEX -> "Extracting with regex…"
                        ExtractionMethod.LLM -> "Extracting with LLM…"
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    if (state.partialOutput.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = state.partialOutput,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                is TextImageUiState.Done -> {
                    if (state.sourceText.isNotBlank() && state.sourceText != textInput) {
                        // If OCR produced text, show it so the user can see what was read
                        OcrSourceTextCard(text = state.sourceText)
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    EntitiesResultCard(entities = state.entities)
                }

                is TextImageUiState.DeepAnalyzing -> {
                    EntitiesResultCard(entities = state.entities)
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        Text(
                            text = "Analyzing deeply…",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    if (state.partialOutput.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = state.partialOutput,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                is TextImageUiState.AnalysisComplete -> {
                    if (state.sourceText.isNotBlank() && state.sourceText != textInput) {
                        OcrSourceTextCard(text = state.sourceText)
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    EntitiesResultCard(entities = state.entities)
                    Spacer(modifier = Modifier.height(16.dp))
                    AnalysisDetailCard(
                        redactedText = state.redactedText,
                        summary = state.summary,
                        analysis = state.analysis,
                    )
                }

                is TextImageUiState.Error -> {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun OcrSourceTextCard(text: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(12.dp),
            )
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "Extracted from image",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun EntitiesResultCard(entities: ExtractedEntities) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(12.dp),
            )
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Extracted Entities",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )

        if (entities.isEmpty) {
            Text(
                text = "No entities found.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            if (entities.phones.isNotEmpty()) {
                EntitySection(label = "Phone Numbers", items = entities.phones)
            }
            if (entities.emails.isNotEmpty()) {
                EntitySection(label = "Emails", items = entities.emails)
            }
            if (entities.urls.isNotEmpty()) {
                EntitySection(label = "URLs", items = entities.urls)
            }
            if (entities.domains.isNotEmpty()) {
                EntitySection(label = "Domains", items = entities.domains)
            }
        }
    }
}

@Composable
private fun EntitySection(label: String, items: List<String>) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.primary,
        )
        items.forEach { item ->
            Text(
                text = item,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun AnalysisDetailCard(
    redactedText: String,
    summary: String,
    analysis: String,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(12.dp),
            )
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Deep Analysis",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )

        if (redactedText.isNotBlank()) {
            Text(
                text = "Redacted Message",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = redactedText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        if (summary.isNotBlank()) {
            Text(
                text = "Research Findings",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = summary,
                style = MaterialTheme.typography.bodySmall,
            )
        }

        if (analysis.isNotBlank()) {
            Text(
                text = "AI Risk Assessment",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = analysis,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
fun PhishingUrlDetectionScreen(viewModel: PhishingDetectionViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    var urlInput by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current

    var webViewContainer by remember { mutableStateOf<FrameLayout?>(null) }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is PhishingUiEffect.InspectUrl -> {
                    val container = webViewContainer ?: return@collect
                    WebsiteInspectorWebView(context, container).inspect(effect.url) { result ->
                        viewModel.onWebInspectionResult(effect.url, result)
                    }
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = androidx.compose.ui.graphics.Color.White)
    ) {
        AndroidView(
            factory = { ctx ->
                FrameLayout(ctx).also {
                    webViewContainer = it
                }
            },
            modifier = Modifier.height(0.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .systemBarsPadding()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Phishing Detection",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = urlInput,
                onValueChange = { urlInput = it },
                label = { Text("Enter URL") },
                placeholder = { Text(text = "https://example.com", color = androidx.compose.ui.graphics.Color.Gray) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Uri,
                    imeAction = ImeAction.Go
                ),
                keyboardActions = KeyboardActions(
                    onGo = {
                        focusManager.clearFocus()
                        viewModel.onScanRequested(urlInput)
                    }
                ),
                enabled = uiState !is PhishingUiState.Loading
                        && uiState !is PhishingUiState.Downloading
                        && uiState !is PhishingUiState.Analyzing
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    focusManager.clearFocus()
                    viewModel.onScanRequested(urlInput)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState !is PhishingUiState.Loading
                        && uiState !is PhishingUiState.Downloading
                        && uiState !is PhishingUiState.Analyzing
            ) {
                Text("Scan")
            }

            Spacer(modifier = Modifier.height(24.dp))

            when (val state = uiState) {
                is PhishingUiState.Idle -> Unit

                is PhishingUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                is PhishingUiState.Downloading -> {
                    MetadataResultCard(metadata = state.metadata)
                    Spacer(modifier = Modifier.height(16.dp))
                    LinearProgressIndicator(
                        progress = { state.progressPercent / 100f },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Downloading model… ${state.progressPercent}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                is PhishingUiState.Analyzing -> {
                    MetadataResultCard(metadata = state.metadata)
                    Spacer(modifier = Modifier.height(16.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outline,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Analyzing…",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        if (state.partialAnalysis.isNotBlank()) {
                            Text(
                                text = state.partialAnalysis,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                is PhishingUiState.AnalysisComplete -> {
                    MetadataResultCard(metadata = state.metadata)
                    Spacer(modifier = Modifier.height(16.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outline,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "AI Analysis",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = state.analysis,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                is PhishingUiState.Error -> {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun MetadataResultCard(metadata: WebsiteMetadata) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Scan Results",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        if (metadata.title.isNotBlank()) {
            MetadataField(label = "Title", value = metadata.title)
        }

        if (metadata.keywords.isNotBlank()) {
            MetadataField(label = "Keywords", value = metadata.keywords)
        }

        val displayDescription = metadata.ogDescription.ifBlank { metadata.description }
        if (displayDescription.isNotBlank()) {
            MetadataField(label = "Description", value = displayDescription)
        }

        if (metadata.ogTitle.isNotBlank() && metadata.ogTitle != metadata.title) {
            MetadataField(label = "OG Title", value = metadata.ogTitle)
        }

        if (metadata.bodyText.isNotBlank()) {
            MetadataField(label = "Page Content Preview", value = metadata.bodyText)
        }
    }
}

@Composable
private fun MetadataField(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = androidx.compose.ui.graphics.Color.Black,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
