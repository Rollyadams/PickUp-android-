package org.dmn.template

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/** One-tap hand-off to Google Maps (or any app that handles navigation intents). */
private fun openNavigation(context: Context, destination: String): Boolean {
    val encoded = Uri.encode(destination)
    val attempts = listOf("google.navigation:q=$encoded", "geo:0,0?q=$encoded")
    for (uriString in attempts) {
        try {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(uriString)))
            return true
        } catch (e: ActivityNotFoundException) {
            // try the next handler
        }
    }
    return false
}

private fun clock(totalSeconds: Int): String =
    "%02d:%02d".format(totalSeconds / 60, totalSeconds % 60)

private val cancelReasons = listOf(
    "Rider not at pickup",
    "Rider asked to cancel",
    "Wrong pickup location",
    "Vehicle problem",
    "Rider not responding",
    "Other"
)

@Composable
private fun NavigateButton(label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(containerColor = PuInk, contentColor = Color.White)
    ) {
        Text("➤  $label", fontSize = 15.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun CircleAction(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(52.dp)
            .clip(CircleShape)
            .background(PuAmber)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = PuAmberInk, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

/** Very small tappable chip used for SOS and Chat during a trip. */
@Composable
private fun TinyChip(label: String, color: Color, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(PuCard)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = color, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun PickupNavigationScreen(
    request: RideRequest,
    onStartTrip: () -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    var arrived by remember { mutableStateOf(false) }
    var waitSeconds by remember { mutableIntStateOf(0) }
    var showCancel by remember { mutableStateOf(false) }
    var reason by remember { mutableStateOf<String?>(null) }
    var showNoMaps by remember { mutableStateOf(false) }
    var showCall by remember { mutableStateOf(false) }
    var showChat by remember { mutableStateOf(false) }

    // The phone's back button does nothing here. Cancelling is only via "Cancel ride".
    BackHandler { }

    // Waiting timer starts the moment the driver taps "I've arrived".
    LaunchedEffect(arrived) {
        if (arrived) {
            while (true) {
                delay(1000)
                waitSeconds++
            }
        }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = PuBg) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(onClick = { reason = null; showCancel = true }) {
                    Text("← Cancel ride", color = PuDanger, fontWeight = FontWeight.Bold)
                }
                if (arrived) {
                    Pill("Waiting ${clock(waitSeconds)}", PuAmber, PuAmberInk)
                } else {
                    Pill("${request.etaMinutes} min away", PuCard, PuInk)
                }
            }
            Spacer(Modifier.height(4.dp))

            InfoCard {
                Row(verticalAlignment = Alignment.Top) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        RiderAvatar(request.riderName, 44.dp)
                        Spacer(Modifier.height(4.dp))
                        Text(request.riderName, color = PuInk, fontSize = 13.sp)
                        Text("★ ${request.riderRating}", color = PuMuted, fontSize = 12.sp)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        AddressRow("A", Color(0xFF3B6BFF), request.pickup, PuInk, 16.sp, true)
                        Spacer(Modifier.height(8.dp))
                        AddressRow("B", Color(0xFF1FA463), request.dropoff, PuMuted, 14.sp)
                    }
                    Spacer(Modifier.width(8.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        CircleAction("Call") { showCall = true }
                        CircleAction("Chat") { showChat = true }
                    }
                }
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Pill(request.paymentMethod, PuChip, PuInk, 11.sp)
                    Spacer(Modifier.width(10.dp))
                    Text(naira(request.fare), color = PuInk, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(12.dp))

            MapPlaceholder(Modifier.weight(1f)) {
                NavigateButton(
                    label = "Navigate",
                    onClick = { if (!openNavigation(context, request.pickup)) showNoMaps = true },
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(12.dp)
                )
            }
            Spacer(Modifier.height(12.dp))

            if (arrived) {
                PrimaryButton("Start trip", onStartTrip)
            } else {
                PrimaryButton("I've arrived", { arrived = true })
            }
        }
    }

    if (showCancel) {
        AlertDialog(
            onDismissRequest = { showCancel = false },
            confirmButton = {
                TextButton(
                    onClick = { showCancel = false; onCancel() },
                    enabled = reason != null
                ) {
                    Text("Cancel ride", color = if (reason != null) PuDanger else PuMuted)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancel = false }) {
                    Text("Keep ride", color = PuAmber)
                }
            },
            title = { Text("Why are you cancelling?") },
            text = {
                Column {
                    cancelReasons.forEach { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { reason = item }
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = reason == item,
                                onClick = null,
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = PuAmber,
                                    unselectedColor = PuMuted
                                )
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(item, color = PuInk, fontSize = 15.sp)
                        }
                    }
                }
            },
            containerColor = PuCard,
            titleContentColor = PuInk,
            textContentColor = PuInk
        )
    }
    if (showNoMaps) {
        SkeletonDialog(message = "No maps app found on this phone.", onOk = { showNoMaps = false })
    }
    if (showCall) {
        SkeletonDialog(message = "Calling ${request.riderName} comes in a later batch.", onOk = { showCall = false })
    }
    if (showChat) {
        SkeletonDialog(message = "Chat with ${request.riderName} comes in a later batch.", onOk = { showChat = false })
    }
}

@Composable
fun TripInProgressScreen(request: RideRequest, onEndTrip: () -> Unit) {
    val context = LocalContext.current
    var seconds by remember { mutableIntStateOf(0) }
    var showSos by remember { mutableStateOf(false) }
    var sosSent by remember { mutableStateOf(false) }
    var showChat by remember { mutableStateOf(false) }
    var showEnd by remember { mutableStateOf(false) }
    var showNoMaps by remember { mutableStateOf(false) }

    // Trip is live: block the system back button so it can't be left by accident.
    BackHandler { }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            seconds++
        }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = PuBg) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Trip in progress", color = PuMuted, fontSize = 14.sp)
                Pill(clock(seconds), PuCard, PuInk)
            }
            Spacer(Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(naira(request.fare), color = PuInk, fontSize = 36.sp, fontWeight = FontWeight.Bold)
                    Text(
                        "${request.riderName} · ${request.paymentMethod}",
                        color = PuMuted, fontSize = 14.sp
                    )
                }
                // SOS and Chat: tiny, above the map, away from the map controls.
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TinyChip("SOS", PuDanger) { showSos = true }
                    TinyChip("Chat", PuInk) { showChat = true }
                }
            }
            Spacer(Modifier.height(12.dp))

            InfoCard {
                AddressRow("A", Color(0xFF3B6BFF), request.pickup, PuMuted, 14.sp)
                Spacer(Modifier.height(8.dp))
                AddressRow("B", Color(0xFF1FA463), request.dropoff, PuInk, 16.sp, true)
            }
            Spacer(Modifier.height(12.dp))

            MapPlaceholder(Modifier.weight(1f)) {
                NavigateButton(
                    label = "Navigate to drop-off",
                    onClick = { if (!openNavigation(context, request.dropoff)) showNoMaps = true },
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(12.dp)
                )
            }
            Spacer(Modifier.height(12.dp))
            PrimaryButton("End trip", { showEnd = true })
        }
    }

    if (showEnd) {
        AlertDialog(
            onDismissRequest = { showEnd = false },
            confirmButton = {
                TextButton(onClick = { showEnd = false; onEndTrip() }) {
                    Text("Yes, end trip", color = PuAmber, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEnd = false }) {
                    Text("Not yet", color = PuMuted)
                }
            },
            title = { Text("End this trip?") },
            text = { Text("Only end the trip once ${request.riderName} has reached ${request.dropoff}.") },
            containerColor = PuCard,
            titleContentColor = PuInk,
            textContentColor = PuMuted
        )
    }
    if (showSos) {
        AlertDialog(
            onDismissRequest = { showSos = false },
            confirmButton = {
                TextButton(onClick = { showSos = false; sosSent = true }) {
                    Text("Send SOS", color = PuDanger)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSos = false }) {
                    Text("Cancel", color = PuMuted)
                }
            },
            title = { Text("Send an SOS?") },
            text = { Text("Skeleton build: later this alerts Pick Up support and shares your live location.") },
            containerColor = PuCard,
            titleContentColor = PuInk,
            textContentColor = PuMuted
        )
    }
    if (sosSent) {
        SkeletonDialog(message = "SOS alert would be sent now.", onOk = { sosSent = false })
    }
    if (showChat) {
        SkeletonDialog(message = "Chat with ${request.riderName} comes in a later batch.", onOk = { showChat = false })
    }
    if (showNoMaps) {
        SkeletonDialog(message = "No maps app found on this phone.", onOk = { showNoMaps = false })
    }
}

@Composable
private fun ReceiptRow(label: String, value: String, strong: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            color = if (strong) PuInk else PuMuted,
            fontSize = if (strong) 18.sp else 14.sp,
            fontWeight = if (strong) FontWeight.Bold else FontWeight.Normal
        )
        Text(
            value,
            color = PuInk,
            fontSize = if (strong) 22.sp else 14.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun TripCompleteScreen(request: RideRequest, onDone: () -> Unit) {
    val km = "%.1f".format(request.distanceKm)
    val isCash = request.paymentMethod == "Cash"
    val vat = vatOn(request.fare)

    // No going back into a finished trip.
    BackHandler { }

    Surface(modifier = Modifier.fillMaxSize(), color = PuBg) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Text("✓ Trip complete", color = PuInk, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))

            InfoCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StopMarker("A", Color(0xFF3B6BFF))
                    Spacer(Modifier.width(10.dp))
                    Text(request.pickup, color = PuInk, fontSize = 15.sp)
                }
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StopMarker("B", Color(0xFF1FA463))
                    Spacer(Modifier.width(10.dp))
                    Text(request.dropoff, color = PuInk, fontSize = 15.sp)
                }
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Pill(request.paymentMethod, PuChip, PuInk, 11.sp)
                    Spacer(Modifier.width(10.dp))
                    Text("$km km · ${request.riderName}", color = PuMuted, fontSize = 13.sp)
                }
            }
            Spacer(Modifier.height(12.dp))

            InfoCard {
                ReceiptRow("Fare", naira(request.fare))
                ReceiptRow("Commission", naira(0))
                ReceiptRow("VAT (7.5%)", "−" + naira(vat))
                ReceiptRow("Daily fee", "Billed once a day")
                Spacer(Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(PuLine)
                )
                Spacer(Modifier.height(6.dp))
                ReceiptRow("You receive", naira(request.fare), strong = true)
            }
            Spacer(Modifier.height(8.dp))
            Text("VAT is taken from your wallet, not from your fare.", color = PuMuted, fontSize = 12.sp)

            Spacer(Modifier.weight(1f))

            Text(
                if (isCash) "Collect ${naira(request.fare)} cash from ${request.riderName}."
                else "Check your bank app for ${naira(request.fare)} from ${request.riderName}.",
                color = PuMuted, fontSize = 13.sp
            )
            Spacer(Modifier.height(10.dp))
            PrimaryButton("Payment confirmed", onDone)
        }
    }
}
