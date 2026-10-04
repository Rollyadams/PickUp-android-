package org.dmn.template

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
            PrimaryButton("Send Code", onSendCode, enabled = phone.length == 11 && phone.startsWith("0"))
            Spacer(Modifier.height(16.dp))
            Text("Build ${appVersion()}", color = PuMuted, fontSize = 11.sp)
        }
    }
}

@Composable
fun CodeEntryScreen(onVerified: () -> Unit, onBack: () -> Unit) {
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

@Composable
fun DriverHomeScreen(
    online: Boolean,
    onOnlineChange: (Boolean) -> Unit,
    onOpenRequest: (String) -> Unit,
    onMenu: () -> Unit
) {
    // Nearest pickup first. With live data this list refreshes as requests change.
    val requests = remember { DriverRepository.requests().sortedBy { it.etaMinutes } }

    val locationOk = rememberAlwaysLocationGranted()
    val requestLocation = rememberLocationRequester()
    var showLocationDialog by remember { mutableStateOf(false) }

    // Going online needs "Allow all the time". Losing it takes the driver offline.
    LaunchedEffect(locationOk) {
        if (!locationOk && online) onOnlineChange(false)
    }

    Surface(modifier = Modifier.fillMaxSize(), color = PuBg) {
        Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MenuButton(onClick = onMenu)
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        if (online) "You're online" else "You're offline",
                        color = PuInk, fontSize = 20.sp, fontWeight = FontWeight.Bold
                    )
                    Text(
                        if (online) "${requests.size} ride requests · nearest first"
                        else "Go online to accept rides",
                        color = PuMuted, fontSize = 13.sp
                    )
                }
                Switch(
                    checked = online,
                    onCheckedChange = { wantOnline ->
                        if (wantOnline && !locationOk) showLocationDialog = true else onOnlineChange(wantOnline)
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = PuAmberInk,
                        checkedTrackColor = PuAmber,
                        uncheckedThumbColor = PuMuted,
                        uncheckedTrackColor = PuCard,
                        uncheckedBorderColor = PuLine
                    )
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentPadding = PaddingValues(vertical = 0.dp)
            ) {
                items(requests) { request ->
                    RequestCard(request) { onOpenRequest(request.id) }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(PuLine)
                    )
                }
            }
            Text(
                "Build ${appVersion()}",
                color = PuMuted, fontSize = 11.sp,
                modifier = Modifier.padding(start = 20.dp, top = 4.dp, bottom = 6.dp)
            )
        }
    }

    if (showLocationDialog) {
        LocationGateDialog(
            onAllow = { requestLocation() },
            onDismiss = { showLocationDialog = false }
        )
    }
}

@Composable
private fun RequestCard(r: RideRequest, onClick: () -> Unit) {
    val km = "%.1f".format(r.distanceKm)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(naira(r.fare), color = PuInk, fontSize = 26.sp, fontWeight = FontWeight.Bold)
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
        Text("From  ${r.pickup}", color = PuInk, fontSize = 15.sp)
        Text("To  ${r.dropoff}", color = PuInk, fontSize = 15.sp)
        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            RiderAvatar(r.riderName, 24.dp)
            Spacer(Modifier.width(8.dp))
            Text(
                "${r.riderName} · ★ ${r.riderRating}",
                color = PuMuted, fontSize = 12.sp
            )
            Spacer(Modifier.weight(1f))
            Pill(r.paymentMethod, PuChip, PuInk, 11.sp)
        }
    }
}

@Composable
fun RequestDetailScreen(request: RideRequest, onBack: () -> Unit, onAccepted: () -> Unit) {
    var dialogMessage by remember { mutableStateOf<String?>(null) }
    val locationOk = rememberAlwaysLocationGranted()
    val requestLocation = rememberLocationRequester()
    var showLocationDialog by remember { mutableStateOf(false) }
    val counters = remember(request) { counterOffers(request.fare) }
    val km = "%.1f".format(request.distanceKm)

    Surface(modifier = Modifier.fillMaxSize(), color = PuBg) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(naira(request.fare), color = PuInk, fontSize = 40.sp, fontWeight = FontWeight.Bold)
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
                Pill(request.paymentMethod, PuChip, PuInk, 11.sp)
            }
            Spacer(Modifier.height(12.dp))
            InfoCard {
                AddressRow("A", Color(0xFF3B6BFF), request.pickup)
                Spacer(Modifier.height(8.dp))
                AddressRow("B", Color(0xFF1FA463), request.dropoff)
            }
            Spacer(Modifier.height(12.dp))

            MapPlaceholder(Modifier.weight(1f))

            Spacer(Modifier.height(12.dp))

            PrimaryButton(
                text = "Accept ${naira(request.fare)}",
                onClick = { if (locationOk) onAccepted() else showLocationDialog = true }
            )

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

            Spacer(Modifier.height(10.dp))
            Button(
                onClick = onBack,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PuChip, contentColor = PuInk)
            ) {
                Text("Ignore", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }

    dialogMessage?.let { message ->
        SkeletonDialog(message = message, onOk = {
            dialogMessage = null
            onBack()
        })
    }

    if (showLocationDialog) {
        LocationGateDialog(
            onAllow = { requestLocation() },
            onDismiss = { showLocationDialog = false }
        )
    }
}
