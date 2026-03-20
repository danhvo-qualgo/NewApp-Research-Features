package com.safeNest.demo.features.home.impl.presentation

import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.safeNest.demo.features.callProtection.impl.presentation.router.CallDetectionDeeplink
import com.safeNest.demo.features.designSystem.theme.DSTheme
import com.safeNest.demo.features.home.impl.presentation.ui.mediaPreview.MediaPreviewScreen
import com.safeNest.demo.features.home.impl.presentation.ui.mediaPreview.MediaType
import com.safeNest.demo.features.home.impl.presentation.ui.home.HomeScreen
import com.safeNest.demo.features.home.impl.presentation.ui.recording.RecordingScreen
import com.safeNest.demo.features.scamAnalyzer.api.router.ScamAnalyzerDeepLink
import com.safeNest.demo.features.urlGuard.api.UrlGuardProvider
import com.uney.core.router.RouterManager
import com.uney.core.router.compose.LocalRouterManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.safeNest.demo.features.home.impl.presentation.ui.settings.CustomPromptScreen

@AndroidEntryPoint
class HomeActivity : ComponentActivity() {

    @Inject
    lateinit var routerManager: RouterManager

    @Inject
    lateinit var urlGuardProvider: UrlGuardProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
        )

        urlGuardProvider.startService(this)
        setContent {
            CompositionLocalProvider(LocalRouterManager provides routerManager) {
                DSTheme {
                    val navController = rememberNavController()
                    
                    NavHost(
                        navController = navController,
                        startDestination = "home"
                    ) {
                        composable("home") {
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
                                onScamAnalyzerClick = {
                                    routerManager.navigate(
                                        this@HomeActivity,
                                        ScamAnalyzerDeepLink.entryPoint()
                                    )
                                },
                                onRecordAudioClick = {
                                    navController.navigate("recording")
                                },
                                onUploadAudioClick = { audioUri ->
                                    navController.navigate("mediaPreview/audio/${Uri.encode(audioUri.toString())}")
                                },
                                onUploadImageClick = { imageUri ->
                                    navController.navigate("mediaPreview/image/${Uri.encode(imageUri.toString())}")
                                },
                                onConfigurePromptClick = {
                                    navController.navigate("customPrompt")
                                }
                            )
                        }
                        
                        composable("recording") {
                            RecordingScreen(
                                onStopRecording = {
                                },
                                onAnalysisSuccess = { audioUri ->
                                    navController.navigate("mediaPreview/audio/${Uri.encode(audioUri.toString())}")
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
                            route = "mediaPreview/{mediaType}/{mediaUri}",
                            arguments = listOf(
                                navArgument("mediaType") { type = NavType.StringType },
                                navArgument("mediaUri") { type = NavType.StringType }
                            )
                        ) { backStackEntry ->
                            val mediaTypeString = backStackEntry.arguments?.getString("mediaType")
                            val mediaUriString = backStackEntry.arguments?.getString("mediaUri")
                            val mediaUri = mediaUriString?.let { Uri.parse(Uri.decode(it)) }
                            val mediaType = when (mediaTypeString) {
                                "image" -> MediaType.IMAGE
                                else -> MediaType.AUDIO
                            }
                            
                            if (mediaUri != null) {
                                MediaPreviewScreen(
                                    mediaUri = mediaUri,
                                    mediaType = mediaType,
                                    context = this@HomeActivity,
                                    onAnalyzeClick = {
                                        navController.popBackStack(route = "home", inclusive = false)
                                        routerManager.navigate(
                                            this@HomeActivity,
                                            ScamAnalyzerDeepLink.entryPoint()
                                        )
                                    },
                                    onDeleteClick = {
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
}