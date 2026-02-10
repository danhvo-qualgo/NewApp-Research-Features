package net.qualgo.safeNest.core.authChallenge.impl.presentation.activity

import android.graphics.Color
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import net.qualgo.safeNest.core.authChallenge.api.presentation.router.AuthChallengeRouterConst
import net.qualgo.safeNest.core.authChallenge.impl.domain.model.AuthChallenge
import net.qualgo.safeNest.core.authChallenge.impl.domain.model.AuthChallengeName
import net.qualgo.safeNest.core.authChallenge.impl.presentation.screen.Screen
import net.qualgo.safeNest.core.authChallenge.impl.presentation.screen.VerifyOtpHelpScreen
import net.qualgo.safeNest.core.authChallenge.impl.presentation.screen.VerifyOtpScreen
import net.qualgo.safeNest.core.authChallenge.impl.presentation.screen.VerifySsoScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AuthChallengeActivity : ComponentActivity() {

    private val viewModel: AuthChallengeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
        )

        kotlin.runCatching {
            viewModel.initAuthChallenge(
                intent.data?.getQueryParameter(AuthChallengeRouterConst.PARAM_AUTH_CHALLENGE)
            )
        }.onFailure {
            finish()
            return
        }

        val showSsoScreenUi = intent.data?.getQueryParameter(
            AuthChallengeRouterConst.PARAM_MOCK_SHOW_SSO_SCREEN_UI
        )?.toBoolean() ?: false

        setContent {
            val navController = rememberNavController()

            NavHost(
                navController = navController,
                startDestination = getScreen(viewModel.authChallenge)
            ) {
                composable<Screen.VerifyOtp> {
                    VerifyOtpScreen(
                        data = viewModel.authChallenge,
                        onNavigateHelp = {
                            navController.navigate(Screen.VerifyOtpHelp)
                        },
                        onNavigateAuthChallenge = {
                            navigateAuthChallenge(navController, it)
                        }
                    )
                }
                composable<Screen.VerifyOtpHelp> {
                    VerifyOtpHelpScreen()
                }
                composable<Screen.VerifySso> {
                    VerifySsoScreen(
                        data = viewModel.authChallenge,
                        showSsoScreenUi = showSsoScreenUi,
                        onNavigateAuthChallenge = {
                            navigateAuthChallenge(navController, it)
                        }
                    )
                }
            }
        }
    }

    private fun getScreen(data: AuthChallenge): Screen {
        return when (data.challengeName) {
            AuthChallengeName.VERIFY_OTP -> Screen.VerifyOtp
            AuthChallengeName.VERIFY_SSO -> Screen.VerifySso
            else -> throw IllegalArgumentException("Invalid authChallengeName: ${data.challengeName}")
        }
    }

    private fun navigateAuthChallenge(navController: NavController, data: AuthChallenge) {
        if (AuthChallengeName.SUCCESS == data.challengeName) {
            setResult(RESULT_OK)
            finish()
        } else {
            viewModel.authChallenge = data
            navController.navigate(getScreen(data)) { popUpTo(0) { inclusive = true } }
        }
    }
}