package com.safeNest.demo.call.main.impl.presentation.ui.home

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.safeNest.demo.call.main.impl.R
import com.safeNest.demo.call.main.impl.presentation.service.recorder.RecorderService

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onGoToWhitelist: () -> Unit,
    onGoToBlacklist: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity
    Column(Modifier.fillMaxSize().background(Color.White).padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = {
                RecorderService.stop(context)
                activity?.finish()
            }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_back),
                    contentDescription = "Back"
                )
            }
            Text(
                text = "Call Detection",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 24.dp)
            )
        }

        Button(onClick = {
            onGoToWhitelist()
        }, modifier = Modifier.padding(vertical = 12.dp)) {
            Text("Whitelist")
        }

        Button(onClick = {
            onGoToBlacklist()
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