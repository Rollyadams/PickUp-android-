package org.dmn.template

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
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

@Composable
fun PickupNavigationScreen(
    request: RideRequest,
    onStartTrip: () -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    var arrived by remember { mutableStateOf(false) }
    var showCancel by remember { mutableStateOf(false) }
    var showNoMaps by remember { mutableStateOf(false) }

    BackHandler { showCancel = true }

    Surface(modifier = Modifier.fillMaxSize(), color = PuNavy) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Text(
                if (arrived) "You've arrived" else "Heading to pickup",
                color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold
            )
            Text(
                if (arrived) "Wait for ${request.riderName}, then start the trip."
                else "${request.riderName} is ${request.etaMinutes} min away from you.",
                color = PuMuted, fontSize = 14.sp
            )
            Spacer(Modifier.height(20.dp))

            InfoCard {
                Text("PICKUP", color = PuMuted, fontSize = 11.sp)
                Text(request.pickup, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(12.dp))
                Text("RIDER", color = PuMuted, fontSize = 11.sp)
                Text(
                    "${request.riderName} · ★ ${request.riderRating} (${request.riderReviews})",
                    color = Color.White, fontSize = 16.sp
                )
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Pill(request.paymentMethod, Color(0xFF2A3A66), Color.White)
                    Spacer(Modifier.width(10.dp))
                    Text(naira(request.fare), color = Color.White, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(16.dp))
            OutlineButton(
                text = "Open in Google Maps",
                onClick = { if (!openNavigation(context, request.pickup)) showNoMaps = true }
            )

            Spacer(Modifier.weight(1f))

            if (arrived) {
                PrimaryButton("Start trip", onStartTrip)
            } else {
                PrimaryButton("I've arrived") { arrived = true }
            }
            TextButton(
                onClick = { showCancel = true },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("Cancel ride", color = PuDanger)
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
}

private fun clock(totalSeconds: Int): String =
    "%02d:%02d".format(totalSeconds / 60, totalSeconds % 60)

@Composable
fun TripInProgressScreen(request: RideRequest, onEndTrip: () -> Unit) {
    val context = LocalContext.current
    var seconds by remember { mutableIntStateOf(0) }
    var showSos by remember { mutableStateOf(false) }
    var sosSent by remember { mutableStateOf(false) }
    var showChat by remember { mutableStateOf(false) }
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
                .padding(horizontal = 20.dp, vertical = 16.dp)
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
            Text(naira(request.fare), color = Color.White, fontSize = 44.sp, fontWeight = FontWeight.Bold)
            Text(
                "${request.riderName} · ${request.paymentMethod}",
                color = PuMuted, fontSize = 14.sp
            )
            Spacer(Modifier.height(20.dp))

            InfoCard {
                Text("FROM", color = PuMuted, fontSize = 11.sp)
                Text(request.pickup, color = Color.White, fontSize = 16.sp)
                Spacer(Modifier.height(12.dp))
                Text("TO", color = PuMuted, fontSize = 11.sp)
                Text(request.dropoff, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(16.dp))
            OutlineButton(
                text = "Navigate to drop-off",
                onClick = { if (!openNavigation(context, request.dropoff)) showNoMaps = true }
            )

            Spacer(Modifier.weight(1f))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlineButton("Chat", { showChat = true }, Modifier.weight(1f), Color.White)
                OutlineButton("SOS", { showSos = true }, Modifier.weight(1f), PuDanger)
            }
            Spacer(Modifier.height(10.dp))
            PrimaryButton("End trip", onEndTrip)
        }
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
