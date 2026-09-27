package org.dmn.template

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PhoneEntryScreen()
        }
    }
}

@Composable
fun PhoneEntryScreen() {
    var phone by remember { mutableStateOf("") }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF101A33)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text("Pick", color = Color.White, fontSize = 34.sp, fontWeight = FontWeight.Bold)
            Text("Up", color = Color(0xFFFF8A1E), fontSize = 34.sp, fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.height(40.dp))

            Text("Enter your phone number", color = Color(0xB3FFFFFF), fontSize = 16.sp)
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                placeholder = { Text("080X XXX XXXX", color = Color(0x61FFFFFF)) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = Color(0xFF16223F),
                    unfocusedContainerColor = Color(0xFF16223F)
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { /* no logic yet */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF8A1E),
                    contentColor = Color(0xFF3A1E00)
                )
            ) {
                Text("Send Code", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}