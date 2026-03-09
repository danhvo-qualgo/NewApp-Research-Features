package com.safeNest.features.call.callDetection.impl.presentation.ui.blacklist

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

@Composable
fun BlacklistScreen(viewModel: BlacklistModel = hiltViewModel()){
    val numbers by viewModel.blacklist.collectAsState()
    val context = LocalContext.current
    val activity = context as? Activity
    var input by remember { mutableStateOf("") }

    Column(Modifier.fillMaxSize().background(Color.White).padding(16.dp)) {
        TextButton(onClick = {
            activity?.finish()
        }) {
            Text(text = "Blacklist", fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 24.dp))
        }
        OutlinedTextField(
            value = input,
            onValueChange = { input = it },
            label = { Text("Blacklist Pattern") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(onClick = {
            viewModel.add(input)
            input = ""
        }, modifier = Modifier.padding(vertical = 12.dp)) {
            Text("Add Blacklist Pattern")
        }

        LazyColumn {
            items(numbers) { number ->
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(8.dp)
                ) {
                    Text(number.pattern)
                    TextButton(onClick = {
                        viewModel.remove(number.pattern)
                    }) {
                        Text("Remove")
                    }
                }
                Spacer(Modifier.fillMaxWidth().height(1.dp).background(Color.LightGray))
            }
        }
    }
}