package com.safeNest.demo.features.home.impl.presentation

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.core.net.toUri
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.safeNest.demo.features.callProtection.impl.presentation.router.CallDetectionDeeplink
import com.safeNest.demo.features.designSystem.theme.DSTheme
import com.safeNest.demo.features.home.impl.presentation.share.ShareData
import com.safeNest.demo.features.home.impl.presentation.share.ShareIntentHandler
import com.safeNest.demo.features.home.impl.presentation.share.ShareType
import com.safeNest.demo.features.home.impl.presentation.ui.home.BottomTab
import com.safeNest.demo.features.home.impl.presentation.ui.home.HomeScreen
import com.safeNest.demo.features.home.impl.presentation.ui.mediaPreview.MediaPreviewScreen
import com.safeNest.demo.features.home.impl.presentation.ui.mediaPreview.MediaType
import com.safeNest.demo.features.home.impl.presentation.ui.recording.RecordingScreen
import com.safeNest.demo.features.home.impl.presentation.ui.settings.CustomPromptScreen
import com.safeNest.demo.features.scamAnalyzer.api.router.ScamAnalyzerDeepLink
import com.safeNest.demo.features.urlGuard.api.UrlGuardProvider
import com.uney.core.router.RouterManager
import com.uney.core.router.compose.LocalRouterManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import javax.inject.Inject

@AndroidEntryPoint
class HomeActivity : ComponentActivity() {

    @Inject
    lateinit var routerManager: RouterManager

    @Inject
    lateinit var urlGuardProvider: UrlGuardProvider

    @Inject
    lateinit var shareIntentHandler: ShareIntentHandler

    private var sharedDataFlow: MutableStateFlow<ShareData?> = MutableStateFlow(null)

    private val tabId = mutableStateOf(BottomTab.Home)
    private val sharedText = MutableStateFlow<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("Home", "onCreate")

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
        )

        urlGuardProvider.startService(this)

        sharedDataFlow.value = shareIntentHandler.extractShareData(intent)

        setContent {
            CompositionLocalProvider(LocalRouterManager provides routerManager) {
                DSTheme {
                    val navController = rememberNavController()

                    LaunchedEffect(Unit) {
                        sharedDataFlow.filterNotNull().collect {
                            handleShareData(it, navController)
                            sharedDataFlow.value = null
                        }
                    }

                    NavHost(
                        navController = navController,
                        startDestination = "home"
                    ) {
                        composable(route = "home") {
                            HomeScreen(
                                onBlocklistClick = {
                                    routerManager.navigate(
                                        this@HomeActivity,
                                        CallDetectionDeeplink.entryPointBlocklist()
                                    )
                                },
                                onWhitelistClick = {
                                    routerManager.navigate(
                                        this@HomeActivity,
                                        CallDetectionDeeplink.entryPointWhitelist()
                                    )
                                },
                                onManageProtectionClick = {
                                    routerManager.navigate(
                                        this@HomeActivity,
                                        CallDetectionDeeplink.entryPoint()
                                    )
                                },
                                onScamAnalyzerClick = { resultKey ->
                                    routerManager.navigate(
                                        this@HomeActivity,
                                        ScamAnalyzerDeepLink.entryPointWithResult(resultKey)
                                    )
                                },
                                onRecordAudioClick = {
                                    navController.navigate("recording")
                                },
                                onUploadAudioClick = { audioUri ->
                                    navController.navigate("mediaPreview/audio/${Uri.encode(audioUri.toString())}") {
                                        launchSingleTop = true
                                    }
                                },
                                onUploadImageClick = { imageUri ->
                                    navController.navigate("mediaPreview/image/${Uri.encode(imageUri.toString())}") {
                                        launchSingleTop = true
                                    }
                                },
                                onConfigurePromptClick = {
                                    navController.navigate("customPrompt")
                                },
                                currentTab = tabId,
                                sharedText = sharedText.collectAsState().value,
                                onConsumeSharedText = { sharedText.value = null }
                            )
                        }

                        composable("recording") {
                            RecordingScreen(
                                onStopRecording = {
                                },
                                onAnalysisSuccess = { audioUri ->
                                    navController.navigate("mediaPreview/audio/${Uri.encode(audioUri.toString())}") {
                                        launchSingleTop = true
                                    }
                                }
                            )
                        }

                        composable("customPrompt") {
                            CustomPromptScreen(
                                onBackClick = {
                                    navController.popBackStack()
                                }
                            )
                        }

                        composable(
                            route = "mediaPreview/{mediaType}/{mediaUri}?autoAnalyze={autoAnalyze}",
                            arguments = listOf(
                                navArgument("mediaType") { type = NavType.StringType },
                                navArgument("mediaUri") { type = NavType.StringType },
                                navArgument("autoAnalyze") {
                                    type = NavType.BoolType
                                    defaultValue = false
                                }
                            )
                        ) { backStackEntry ->
                            val mediaTypeString = backStackEntry.arguments?.getString("mediaType")
                            val mediaUriString = backStackEntry.arguments?.getString("mediaUri")
                            val autoAnalyze =
                                backStackEntry.arguments?.getBoolean("autoAnalyze") ?: false
                            val mediaUri = mediaUriString?.let { Uri.decode(it).toUri() }
                            val mediaType = when (mediaTypeString) {
                                "image" -> MediaType.IMAGE
                                else -> MediaType.AUDIO
                            }

                            if (mediaUri != null) {
                                MediaPreviewScreen(
                                    mediaUri = mediaUri,
                                    mediaType = mediaType,
                                    autoAnalyze = autoAnalyze,
                                    onAnalyzeClick = { resultKey ->
                                        routerManager.navigate(
                                            this@HomeActivity,
                                            ScamAnalyzerDeepLink.entryPointWithResult(resultKey)
                                        )
                                    },
                                    onDeleteClick = {
                                        navController.popBackStack()
                                    },
                                    onBackClick = {
                                        navController.popBackStack()
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Log.d("Home", "onNewIntent")
        setIntent(intent)

        sharedDataFlow.value = shareIntentHandler.extractShareData(intent)
    }

    override fun onStop() {
        Log.d("Home", "onStop")
        super.onStop()
    }

    override fun onDestroy() {
        Log.d("Home", "onDestroy")
        super.onDestroy()
    }

    private suspend fun handleShareData(shareData: ShareData, navController: NavHostController) {
        Log.d("###", "Handle share data $shareData")

        when (shareData) {
            is ShareData.Text -> {
                tabId.value = BottomTab.Tools
                sharedText.value = shareData.text
            }

            is ShareData.Audio -> {
                shareIntentHandler.copyToAppStorage(
                    shareData.uri,
                    ShareType.AUDIO
                )?.let {
                    navController.navigate("mediaPreview/audio/${Uri.encode(it.toString())}?autoAnalyze=true") {
                        launchSingleTop = true
                    }
                }
            }

            is ShareData.Image -> {
                shareIntentHandler.copyToAppStorage(
                    shareData.uri,
                    ShareType.IMAGE
                )?.let {
                    navController.navigate("mediaPreview/image/${Uri.encode(it.toString())}?autoAnalyze=true") {
                        launchSingleTop = true
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        urlGuardProvider.startService(this)
    }
}