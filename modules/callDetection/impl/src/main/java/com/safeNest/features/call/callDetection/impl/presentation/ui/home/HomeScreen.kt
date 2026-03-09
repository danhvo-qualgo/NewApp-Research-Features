package com.safeNest.features.call.callDetection.impl.presentation.ui.home

import android.app.Activity
import android.app.role.RoleManager
import android.content.Intent
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import android.provider.Settings
import android.telecom.TelecomManager
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat.getSystemService
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.safeNest.features.call.callDetection.impl.presentation.service.recorder.RecorderService

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onGoToWhitelist: () -> Unit,
    onGoToBacklist: () -> Unit
){
    val context = LocalContext.current
    val activity = context as? Activity
    Column(Modifier.fillMaxSize().background(Color.White).padding(16.dp)) {
        TextButton(onClick = {
            RecorderService.stop(context)
            activity?.finish()
        }) {
            Text(text = "Call Detection", fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 24.dp))
        }

        Button(onClick = {
            onGoToWhitelist()
        }, modifier = Modifier.padding(vertical = 12.dp)) {
            Text("Whitelist")
        }

        Button(onClick = {
            onGoToBacklist()
        }, modifier = Modifier.padding(vertical = 12.dp)) {
            Text("Blacklist")
        }

//        Button(onClick = {
//            RecorderService.start(context)
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//
//                val roleManager = getSystemService(activity!!, RoleManager::class.java)
//
//                if (roleManager?.isRoleHeld(RoleManager.ROLE_CALL_REDIRECTION) == false) {
//
//                    val intent =
//                        roleManager.createRequestRoleIntent(
//                            RoleManager.ROLE_CALL_REDIRECTION
//                        )
//
//                    activity.startActivity(intent)
//                }
//            }
//
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//                val roleManager = context.getSystemService(RoleManager::class.java)
//
//                if (!roleManager.isRoleHeld(RoleManager.ROLE_DIALER)) {
//                    val intent =
//                        roleManager.createRequestRoleIntent(RoleManager.ROLE_DIALER)
//
//                    context.startActivity(intent)
//                }
//            } else {
//                context.startActivity(
//                    Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS)
//                )
//            }
//        }, modifier = Modifier.padding(vertical = 12.dp)) {
//            Text("Set Default")
//        }
    }
}