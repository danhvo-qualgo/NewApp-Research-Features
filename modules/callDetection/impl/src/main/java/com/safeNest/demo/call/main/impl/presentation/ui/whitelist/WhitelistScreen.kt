package com.safeNest.demo.call.main.impl.presentation.ui.whitelist

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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

@Composable
fun WhitelistScreen(
    viewModel: WhitelistModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val numbers by viewModel.whitelist.collectAsState()
    val context = LocalContext.current
    val activity = context as? Activity
    var input by remember { mutableStateOf("") }
    val isEnable by viewModel.isEnable.collectAsState()

    Column(Modifier.fillMaxSize().background(Color.White).padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { onBack() }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_back),
                    contentDescription = "Back"
                )
            }
            Text(
                text = "Whitelist",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 24.dp)
            )
        }

        Button(onClick = {
            if (isEnable) {
                viewModel.enable(false)
            } else {
                viewModel.enable(true)
            }
        }, modifier = Modifier.padding(vertical = 12.dp)) {
            if (isEnable) {
                Text("Disable Whitelist")
            } else {
                Text("Enable Whitelist")
            }
        }

        if (isEnable) {
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                label = { Text("Phone Number") },
                modifier = Modifier.fillMaxWidth()
            )

            Button(onClick = {
                viewModel.add(input)
                input = ""
            }, modifier = Modifier.padding(vertical = 12.dp)) {
                Text("Add to Whitelist")
            }

            LazyColumn {
                items(numbers) { number ->
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().padding(8.dp)
                    ) {
                        Text(number.phoneNumber)
                        TextButton(onClick = {
                            viewModel.remove(number.phoneNumber)
                        }) {
                            Text("Remove")
                        }
                    }
                    Spacer(Modifier.fillMaxWidth().height(1.dp).background(Color.LightGray))
                }
            }
        }
    }
}