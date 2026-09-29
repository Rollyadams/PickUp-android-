package org.dmn.template

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
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
                onValueChange = { phone = it },
                placeholder = { Text("080X XXX XXXX", color = Color(0x61FFFFFF)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth(),
                colors = fieldColors()
            )
            Spacer(Modifier.height(24.dp))
            PrimaryButton("Send Code", onSendCode)
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
fun DriverHomeScreen(onOpenRequest: (String) -> Unit) {
    var online by rememberSaveable { mutableStateOf(false) }
    val requests = remember { DriverRepository.requests() }

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
                        if (online) "${requests.size} ride requests nearby"
                        else "Go online to see ride requests",
                        color = PuMuted, fontSize = 13.sp
                    )
                }
                Switch(
                    checked = online,
                    onCheckedChange = { online = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = PuAmberInk,
                        checkedTrackColor = PuAmber,
                        uncheckedThumbColor = Color(0x99FFFFFF),
                        uncheckedTrackColor = PuNavy2,
                        uncheckedBorderColor = Color(0x66FFFFFF)
                    )
                )
            }

            if (online) {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(requests) { request ->
                        RequestCard(request) { onOpenRequest(request.id) }
                    }
                }
            } else {
                Box(
                    modifier = Modifier.fillMaxWidth().weight(1f).padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Flip the switch above when you're ready to drive.",
                        color = PuMuted, textAlign = TextAlign.Center
                    )
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
                if (r.isFairFare) Pill("Fair fare", PuAmber, PuAmberInk)
            }
            Spacer(Modifier.height(2.dp))
            Text(
                "${naira(perKm(r))}/km · $km km · pickup in ${r.etaMinutes} min",
                color = PuMuted, fontSize = 13.sp
            )
            Spacer(Modifier.height(12.dp))
            Text("From  ${r.pickup}", color = Color.White, fontSize = 15.sp)
            Text("To  ${r.dropoff}", color = Color.White, fontSize = 15.sp)
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Pill(r.paymentMethod, Color(0xFF2A3A66), Color.White)
                Spacer(Modifier.width(10.dp))
                Text(
                    "${r.riderName} · ★ ${r.riderRating} (${r.riderReviews})",
                    color = PuMuted, fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
fun RequestDetailScreen(request: RideRequest, onBack: () -> Unit, onAccepted: () -> Unit) {
    var showCounter by remember { mutableStateOf(false) }
    var selectedCounter by remember { mutableStateOf<Int?>(null) }
    var dialogMessage by remember { mutableStateOf<String?>(null) }

    // Counter buttons sit around the fair fare (-10%, fair, +10%), never above +10%.
    val counterOptions = remember(request) {
        listOf(90, 100, 110)
            .map { pct -> roundTo100(request.fairFare * pct / 100) }
            .filter { it != request.fare }
            .distinct()
    }
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
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(naira(request.fare), color = Color.White, fontSize = 40.sp, fontWeight = FontWeight.Bold)
                if (request.isFairFare) {
                    Spacer(Modifier.width(12.dp))
                    Pill("Fair fare", PuAmber, PuAmberInk)
                }
            }
            Text(
                "${naira(perKm(request))}/km · $km km · pickup in ${request.etaMinutes} min",
                color = PuMuted, fontSize = 14.sp
            )
            Spacer(Modifier.height(20.dp))

            InfoCard {
                Text("PICKUP", color = PuMuted, fontSize = 11.sp)
                Text(request.pickup, color = Color.White, fontSize = 16.sp)
                Spacer(Modifier.height(12.dp))
                Text("DROP-OFF", color = PuMuted, fontSize = 11.sp)
                Text(request.dropoff, color = Color.White, fontSize = 16.sp)
            }
            Spacer(Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Pill(request.paymentMethod, Color(0xFF2A3A66), Color.White)
                Spacer(Modifier.width(10.dp))
                Text(
                    "${request.riderName} · ★ ${request.riderRating} (${request.riderReviews})",
                    color = PuMuted, fontSize = 14.sp
                )
            }

            Spacer(Modifier.weight(1f))

            if (showCounter) {
                Text("Your counter offer", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    counterOptions.forEach { amount ->
                        val selected = selectedCounter == amount
                        OutlinedButton(
                            onClick = { selectedCounter = amount },
                            modifier = Modifier.weight(1f),
                            border = BorderStroke(1.dp, if (selected) PuAmber else Color(0x66FFFFFF)),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (selected) Color(0x33FF8A1E) else Color.Transparent
                            )
                        ) {
                            Text(naira(amount), color = if (selected) PuAmber else Color.White)
                        }
                    }
                }
                Spacer(Modifier.height(10.dp))
                PrimaryButton(
                    text = "Send counter",
                    enabled = selectedCounter != null,
                    onClick = {
                        selectedCounter?.let { dialogMessage = "Counter of ${naira(it)} sent." }
                    }
                )
            } else {
                PrimaryButton(
                    text = "Accept ${naira(request.fare)}",
                    onClick = onAccepted
                )
            }
            Spacer(Modifier.height(10.dp))
            OutlineButton(
                text = if (showCounter) "Cancel counter" else "Counter offer",
                onClick = {
                    showCounter = !showCounter
                    selectedCounter = null
                }
            )
        }
    }

    dialogMessage?.let { message ->
        SkeletonDialog(message = message, onOk = {
            dialogMessage = null
            onBack()
        })
    }
}
