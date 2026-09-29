package org.dmn.template

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PhoneEntryScreen(onSendCode: () -> Unit) {
    var phone by remember { mutableStateOf("") }

    Surface(modifier = Modifier.fillMaxSize(), color = PuNavy) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .imePadding()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text("Pick", color = Color.White, fontSize = 34.sp, fontWeight = FontWeight.Bold)
            Text("Up", color = PuAmber, fontSize = 34.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(40.dp))
            Text("Enter your phone number", color = PuMuted, fontSize = 16.sp)
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = phone,
                onValueChange = { input ->
                    if (input.length <= 11 && input.all { it.isDigit() }) phone = input
                },
                placeholder = { Text("080X XXX XXXX", color = Color(0x61FFFFFF)) },
                supportingText = { Text("${phone.length}/11 digits", color = PuMuted) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth(),
                colors = fieldColors()
            )
            Spacer(Modifier.height(24.dp))
            PrimaryButton("Send Code", onSendCode, enabled = phone.length == 11 && phone.startsWith("0"))
        }
    }
}

@Composable
fun CodeEntryScreen(onVerified: () -> Unit, onBack: () -> Unit) {
    var code by remember { mutableStateOf("") }

    Surface(modifier = Modifier.fillMaxSize(), color = PuNavy) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .imePadding()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text("Enter the code", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text("Skeleton build: any code works for now.", color = PuMuted, fontSize = 14.sp)
            Spacer(Modifier.height(20.dp))
            OutlinedTextField(
                value = code,
                onValueChange = { input ->
                    if (input.length <= 6 && input.all { it.isDigit() }) code = input
                },
                placeholder = { Text("6-digit code", color = Color(0x61FFFFFF)) },
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

@Composable
fun DriverHomeScreen(
    online: Boolean,
    onOnlineChange: (Boolean) -> Unit,
    onOpenRequest: (String) -> Unit
) {
    // Nearest pickup first. With live data this list refreshes as requests change.
    val requests = remember { DriverRepository.requests().sortedBy { it.etaMinutes } }

    Surface(modifier = Modifier.fillMaxSize(), color = PuNavy) {
        Column(modifier = Modifier.fillMaxSize().systemBarsPadding()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        if (online) "You're online" else "You're offline",
                        color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold
                    )
                    Text(
                        if (online) "${requests.size} ride requests · nearest first"
                        else "Go online to accept rides",
                        color = PuMuted, fontSize = 13.sp
                    )
                }
                Switch(
                    checked = online,
                    onCheckedChange = onOnlineChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = PuAmberInk,
                        checkedTrackColor = PuAmber,
                        uncheckedThumbColor = Color(0x99FFFFFF),
                        uncheckedTrackColor = PuNavy2,
                        uncheckedBorderColor = Color(0x66FFFFFF)
                    )
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(requests) { request ->
                    RequestCard(request) { onOpenRequest(request.id) }
                }
            }
        }
    }
}

@Composable
private fun RequestCard(r: RideRequest, onClick: () -> Unit) {
    val km = "%.1f".format(r.distanceKm)

    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PuNavy2)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(naira(r.fare), color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                if (isFairRate(r)) Pill("Fair fare", PuAmber, PuAmberInk)
            }
            Spacer(Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "${naira(perKm(r))}/km",
                    color = PuAmber, fontSize = 18.sp, fontWeight = FontWeight.Bold
                )
                Text(
                    "  ·  $km km  ·  ${r.etaMinutes} min away",
                    color = PuMuted, fontSize = 13.sp
                )
            }
            Spacer(Modifier.height(10.dp))
            Text("From  ${r.pickup}", color = Color.White, fontSize = 15.sp)
            Text("To  ${r.dropoff}", color = Color.White, fontSize = 15.sp)
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                RiderAvatar(r.riderName, 24.dp)
                Spacer(Modifier.width(8.dp))
                Text(
                    "${r.riderName} · ★ ${r.riderRating}",
                    color = PuMuted, fontSize = 12.sp
                )
                Spacer(Modifier.weight(1f))
                Pill(r.paymentMethod, Color(0xFF2A3A66), Color.White, 11.sp)
            }
        }
    }
}

@Composable
fun RequestDetailScreen(request: RideRequest, onBack: () -> Unit, onAccepted: () -> Unit) {
    var dialogMessage by remember { mutableStateOf<String?>(null) }
    val counters = remember(request) { counterOffers(request.fare) }
    val km = "%.1f".format(request.distanceKm)

    Surface(modifier = Modifier.fillMaxSize(), color = PuNavy) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            TextButton(onClick = onBack) {
                Text("← Back to requests", color = PuMuted)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(naira(request.fare), color = Color.White, fontSize = 40.sp, fontWeight = FontWeight.Bold)
                if (isFairRate(request)) {
                    Spacer(Modifier.width(12.dp))
                    Pill("Fair fare", PuAmber, PuAmberInk)
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "${naira(perKm(request))}/km",
                    color = PuAmber, fontSize = 20.sp, fontWeight = FontWeight.Bold
                )
                Text(
                    "  ·  $km km  ·  ${request.etaMinutes} min away",
                    color = PuMuted, fontSize = 14.sp
                )
            }
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                RiderAvatar(request.riderName, 32.dp)
                Spacer(Modifier.width(10.dp))
                Text(
                    "${request.riderName} · ★ ${request.riderRating} (${request.riderReviews})",
                    color = PuMuted, fontSize = 14.sp
                )
                Spacer(Modifier.weight(1f))
                Pill(request.paymentMethod, Color(0xFF2A3A66), Color.White, 11.sp)
            }
            Spacer(Modifier.height(12.dp))

            MapPlaceholder(Modifier.weight(1f))

            Spacer(Modifier.height(12.dp))
            InfoCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StopMarker("A", Color(0xFF3B6BFF))
                    Spacer(Modifier.width(10.dp))
                    Text(request.pickup, color = Color.White, fontSize = 15.sp)
                }
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StopMarker("B", Color(0xFF1FA463))
                    Spacer(Modifier.width(10.dp))
                    Text(request.dropoff, color = Color.White, fontSize = 15.sp)
                }
            }
            Spacer(Modifier.height(12.dp))

            PrimaryButton(text = "Accept ${naira(request.fare)}", onClick = onAccepted)

            if (counters.isNotEmpty()) {
                Spacer(Modifier.height(10.dp))
                Text("Or ask for more", color = PuMuted, fontSize = 12.sp)
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    counters.forEach { amount ->
                        OutlineButton(
                            text = naira(amount),
                            onClick = {
                                dialogMessage = "Counter of ${naira(amount)} sent to ${request.riderName}."
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }

    dialogMessage?.let { message ->
        SkeletonDialog(message = message, onOk = {
            dialogMessage = null
            onBack()
        })
    }
}
