package com.safeNest.demo.features.callProtection.impl.presentation.ui.blacklist

import android.app.Activity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.safeNest.demo.features.callProtection.impl.R
import com.safeNest.demo.features.callProtection.impl.presentation.ui.home.BackgroundLight
import com.safeNest.demo.features.callProtection.impl.presentation.ui.home.InfoBoxBg
import com.safeNest.demo.features.callProtection.impl.presentation.ui.home.InfoBoxBorder
import com.safeNest.demo.features.callProtection.impl.presentation.ui.home.PrimaryPurple
import com.safeNest.demo.features.callProtection.impl.presentation.ui.home.RedIconBg
import com.safeNest.demo.features.callProtection.impl.presentation.ui.home.RedIconColor
import com.safeNest.demo.features.callProtection.impl.presentation.ui.home.TextDark
import com.safeNest.demo.features.callProtection.impl.presentation.ui.home.TextGray

@Composable
fun BlacklistScreen(
    viewModel: BlocklistViewModel = hiltViewModel(),
    onBack: () -> Unit
){
    val numbers by viewModel.blacklist.collectAsState()
    val isEnable by viewModel.isEnable.collectAsState()
    val context = LocalContext.current
    val activity = context as? Activity
    var input by remember { mutableStateOf("") }

    Column(Modifier.fillMaxSize().background(Color.White).padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { onBack() }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_back),
                    contentDescription = "Back"
                )
            }
            Text(text = "Blacklist", fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 24.dp))
        }

        Button(onClick = {
            if(isEnable) {
                viewModel.enable(false)
            } else {
                viewModel.enable(true)
            }
        }, modifier = Modifier.padding(vertical = 12.dp)) {
            if(isEnable) {
                Text("Disable Blacklist")
            } else {
                Text("Enable Blacklist")
            }
        }

        if (isEnable) {
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                label = { Text("Blacklist Pattern") },
                modifier = Modifier.fillMaxWidth()
            )

            Button(onClick = {
//                viewModel.add(input)
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
}


@Composable
fun BlocklistScreen(
    viewModel: BlocklistViewModel = hiltViewModel()
) {
    val isEnable by viewModel.isEnable.collectAsState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Master Toggle Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(48.dp).background(BackgroundLight, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.Public, contentDescription = "Globe", tint = PrimaryPurple)
                }
                Column(modifier = Modifier.weight(1f).padding(horizontal = 16.dp)) {
                    Text("Blocklist Protection", fontWeight = FontWeight.Bold, color = TextDark, fontSize = 16.sp)
                    Text("Automatically block calls from unknown patterns", color = TextGray, fontSize = 13.sp, lineHeight = 18.sp)
                }
                Switch(
                    checked = isEnable,
                    onCheckedChange = {
                        viewModel.enable(it)
                    },
                    colors = SwitchDefaults.colors(checkedTrackColor = PrimaryPurple)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text("Blocked Patterns (3 active)", color = TextDark, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(16.dp))

        // Blocked Patterns (Individual Cards)
        BlockedPatternItem("+44 (0) 800***")
        Spacer(modifier = Modifier.height(12.dp))
        BlockedPatternItem("+1 900*")
        Spacer(modifier = Modifier.height(12.dp))
        BlockedPatternItem("Unknown CID")

        Spacer(modifier = Modifier.height(24.dp))

        // Info Box
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = InfoBoxBg),
            border = BorderStroke(1.dp, InfoBoxBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(modifier = Modifier.padding(16.dp)) {
                Icon(Icons.Rounded.Shield, contentDescription = "Info", tint = PrimaryPurple, modifier = Modifier.padding(top = 2.dp))
                Spacer(modifier = Modifier.width(12.dp))
                val infoText = buildAnnotatedString {
                    append("Incoming calls matching these patterns will be automatically silenced and logged as blocked. You can view blocked calls in your ")
                    withStyle(style = SpanStyle(color = PrimaryPurple, textDecoration = TextDecoration.Underline, fontWeight = FontWeight.Medium)) {
                        append("Call History.")
                    }
                }
                Text(text = infoText, color = TextDark, fontSize = 14.sp, lineHeight = 20.sp)
            }
        }
        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
private fun BlockedPatternItem(pattern: String) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(40.dp).background(RedIconBg, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.Block, contentDescription = "Block", tint = RedIconColor, modifier = Modifier.size(20.dp))
            }
            Column(modifier = Modifier.weight(1f).padding(horizontal = 16.dp)) {
                Text(pattern, fontWeight = FontWeight.Bold, color = TextDark, fontSize = 16.sp)
                Text("Auto block from unknown patterns", color = TextGray, fontSize = 13.sp)
            }
            Icon(Icons.Rounded.DeleteOutline, contentDescription = "Delete", tint = TextGray, modifier = Modifier.clickable { })
        }
    }
}