package com.safeNest.demo.features.callProtection.impl.presentation.ui.whitelist

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PhoneCallback
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.safeNest.demo.features.callProtection.impl.presentation.ui.home.BackgroundLight
import com.safeNest.demo.features.callProtection.impl.presentation.ui.home.PrimaryPurple
import com.safeNest.demo.features.callProtection.impl.presentation.ui.home.PurpleIconBg
import com.safeNest.demo.features.callProtection.impl.presentation.ui.home.TextDark
import com.safeNest.demo.features.callProtection.impl.presentation.ui.home.TextGray

@Composable
fun WhitelistScreen(
    viewModel: WhitelistViewModel = hiltViewModel()
) {
    val allowedContacts by viewModel.whitelist.collectAsState()
    val isEnable by viewModel.isEnable.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Whitelist Master Toggle Card
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
                    Icon(Icons.Rounded.PhoneCallback, contentDescription = "Phone", tint = PrimaryPurple)
                }
                Column(modifier = Modifier.weight(1f).padding(horizontal = 16.dp)) {
                    Text("Whitelist Protection", fontWeight = FontWeight.Bold, color = TextDark, fontSize = 16.sp)
                    Text("Only allowed contacts can reach you", color = TextGray, fontSize = 13.sp, lineHeight = 18.sp)
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
        if (isEnable)
            Text("Allowed Contacts", color = TextDark, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(16.dp))

        if (allowedContacts.isNotEmpty() && isEnable)
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    allowedContacts.forEach { contact ->
                        ContactItem(name = contact.name, phone = contact.phoneNumber) {
                            viewModel.remove(contact.phoneNumber)
                        }
                    }
                }
            }

        Spacer(modifier = Modifier.height(80.dp))
    }
}


@Composable
fun ContactItem(name: String, phone: String, onDelete: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(40.dp).background(PurpleIconBg, RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Rounded.Person, contentDescription = "Person", tint = PrimaryPurple, modifier = Modifier.size(20.dp))
        }
        Column(modifier = Modifier.weight(1f).padding(horizontal = 16.dp)) {
            Text(name, fontWeight = FontWeight.Bold, color = TextDark, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(phone, color = TextGray, fontSize = 13.sp)
        }
        Icon(Icons.Rounded.DeleteOutline, contentDescription = "Delete", tint = TextGray, modifier = Modifier.clickable {
            onDelete()
        })
    }
}