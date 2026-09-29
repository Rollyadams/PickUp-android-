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

@Composable
private fun NavigateButton(label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = PuNavy)
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
    var showNoMaps by remember { mutableStateOf(false) }
    var showCall by remember { mutableStateOf(false) }
    var showChat by remember { mutableStateOf(false) }

    BackHandler { showCancel = true }

    // Waiting timer starts the moment the driver taps "I've arrived".
    LaunchedEffect(arrived) {
        if (arrived) {
            while (true) {
                delay(1000)
                waitSeconds++
            }
        }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = PuNavy) {
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
                TextButton(onClick = { showCancel = true }) {
                    Text("← Cancel ride", color = PuDanger, fontWeight = FontWeight.Bold)
                }
                if (arrived) {
                    Pill("Arrived", PuAmber, PuAmberInk)
                } else {
                    Pill("${request.etaMinutes} min away", PuNavy2, Color.White)
                }
            }
            Spacer(Modifier.height(4.dp))

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
            InfoCard {
                Row(verticalAlignment = Alignment.Top) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        RiderAvatar(request.riderName, 44.dp)
                        Spacer(Modifier.height(4.dp))
                        Text(request.riderName, color = Color.White, fontSize = 13.sp)
                        Text("★ ${request.riderRating}", color = PuMuted, fontSize = 12.sp)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            StopMarker("A", Color(0xFF3B6BFF))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                request.pickup,
                                color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            StopMarker("B", Color(0xFF1FA463))
                            Spacer(Modifier.width(8.dp))
                            Text(request.dropoff, color = PuMuted, fontSize = 14.sp)
                        }
                    }
                    Spacer(Modifier.width(8.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        CircleAction("Call") { showCall = true }
                        CircleAction("Chat") { showChat = true }
                    }
                }
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Pill(request.paymentMethod, Color(0xFF2A3A66), Color.White, 11.sp)
                    Spacer(Modifier.width(10.dp))
                    Text(naira(request.fare), color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.weight(1f))
                    if (arrived) {
                        Pill("Waiting ${clock(waitSeconds)}", PuAmber, PuAmberInk)
                    }
                }
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
                TextButton(onClick = { showCancel = false; onCancel() }) {
                    Text("Cancel ride", color = PuDanger)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancel = false }) {
                    Text("Keep ride", color = PuAmber)
                }
            },
            title = { Text("Cancel this ride?") },
            text = { Text("The rider will be told you cancelled.") },
            containerColor = PuNavy2,
            titleContentColor = Color.White,
            textContentColor = PuMuted
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

    Surface(modifier = Modifier.fillMaxSize(), color = PuNavy) {
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
                Pill(clock(seconds), PuNavy2, Color.White)
            }
            Spacer(Modifier.height(8.dp))

            // SOS and Chat live at the top, well away from the map.
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlineButton("SOS", { showSos = true }, Modifier.weight(1f), PuDanger)
                OutlineButton("Chat", { showChat = true }, Modifier.weight(1f), Color.White)
            }
            Spacer(Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.Bottom) {
                Text(naira(request.fare), color = Color.White, fontSize = 36.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(12.dp))
                Text(
                    "${request.riderName} · ${request.paymentMethod}",
                    color = PuMuted, fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
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
            InfoCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StopMarker("A", Color(0xFF3B6BFF))
                    Spacer(Modifier.width(10.dp))
                    Text(request.pickup, color = PuMuted, fontSize = 14.sp)
                }
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StopMarker("B", Color(0xFF1FA463))
                    Spacer(Modifier.width(10.dp))
                    Text(
                        request.dropoff,
                        color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold
                    )
                }
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
            containerColor = PuNavy2,
            titleContentColor = Color.White,
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
            containerColor = PuNavy2,
            titleContentColor = Color.White,
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
fun TripCompleteScreen(request: RideRequest, onDone: () -> Unit) {
    var confirmed by remember { mutableStateOf(false) }
    val km = "%.1f".format(request.distanceKm)
    val isCash = request.paymentMethod == "Cash"

    BackHandler { }

    Surface(modifier = Modifier.fillMaxSize(), color = PuNavy) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Text("Trip complete", color = PuMuted, fontSize = 14.sp)
            Text(naira(request.fare), color = Color.White, fontSize = 44.sp, fontWeight = FontWeight.Bold)
            Text("$km km · ${request.pickup} → ${request.dropoff}", color = PuMuted, fontSize = 14.sp)
            Spacer(Modifier.height(20.dp))

            InfoCard {
                Text("PAYMENT · ${request.paymentMethod.uppercase()}", color = PuMuted, fontSize = 11.sp)
                Spacer(Modifier.height(6.dp))
                Text(
                    if (isCash) "Collect ${naira(request.fare)} cash from ${request.riderName}."
                    else "${request.riderName} should transfer ${naira(request.fare)} to your bank account. " +
                        "Check your bank app before confirming.",
                    color = Color.White, fontSize = 16.sp
                )
            }
            Spacer(Modifier.height(12.dp))
            Text(
                "Confirming creates the payment record used if there is ever a dispute.",
                color = PuMuted, fontSize = 12.sp
            )

            Spacer(Modifier.weight(1f))

            PrimaryButton(
                text = if (confirmed) "Payment confirmed ✓" else "I've received ${naira(request.fare)}",
                enabled = !confirmed,
                onClick = { confirmed = true }
            )
            Spacer(Modifier.height(10.dp))
            if (confirmed) {
                OutlineButton("Done", onDone)
            } else {
                Text(
                    "Not received yet? Wait for the rider before finishing.",
                    color = PuMuted, fontSize = 12.sp,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}
