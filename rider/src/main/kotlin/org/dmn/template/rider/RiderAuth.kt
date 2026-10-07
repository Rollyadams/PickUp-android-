package org.dmn.template.rider

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.dmn.template.PrimaryButton
import org.dmn.template.PuAmber
import org.dmn.template.PuBg
import org.dmn.template.PuInk
import org.dmn.template.PuMuted
import org.dmn.template.appVersion
import org.dmn.template.fieldColors

@Composable
fun RiderPhoneScreen(onSendCode: (String) -> Unit) {
    var phone by remember { mutableStateOf("") }

    Surface(modifier = Modifier.fillMaxSize(), color = PuBg) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .imePadding()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text("Pick", color = PuInk, fontSize = 34.sp, fontWeight = FontWeight.Bold)
            Text("Up", color = PuAmber, fontSize = 34.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(40.dp))
            Text("Enter your phone number", color = PuMuted, fontSize = 16.sp)
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = phone,
                onValueChange = { input ->
                    if (input.length <= 11 && input.all { it.isDigit() }) phone = input
                },
                placeholder = { Text("080X XXX XXXX", color = PuMuted) },
                supportingText = { Text("${phone.length}/11 digits", color = PuMuted) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth(),
                colors = fieldColors()
            )
            Spacer(Modifier.height(24.dp))
            PrimaryButton(
                "Send Code",
                { onSendCode(phone) },
                enabled = phone.length == 11 && phone.startsWith("0")
            )
            Spacer(Modifier.height(16.dp))
            Text("Build ${appVersion()}", color = PuMuted, fontSize = 11.sp)
        }
    }
}

@Composable
fun RiderCodeScreen(onVerified: () -> Unit, onBack: () -> Unit) {
    var code by remember { mutableStateOf("") }

    Surface(modifier = Modifier.fillMaxSize(), color = PuBg) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .imePadding()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text("Enter the code", color = PuInk, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text("Skeleton build: any code works for now.", color = PuMuted, fontSize = 14.sp)
            Spacer(Modifier.height(20.dp))
            OutlinedTextField(
                value = code,
                onValueChange = { input ->
                    if (input.length <= 6 && input.all { it.isDigit() }) code = input
                },
                placeholder = { Text("6-digit code", color = PuMuted) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                colors = fieldColors()
            )
            Spacer(Modifier.height(24.dp))
            PrimaryButton("Verify", onVerified)
            Spacer(Modifier.height(8.dp))
            TextButton(onClick = { /* no logic yet */ }) {
                Text("Resend code", color = PuAmber)
            }
            TextButton(onClick = onBack) {
                Text("Change number", color = PuMuted)
            }
        }
    }
}
