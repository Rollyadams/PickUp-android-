package org.dmn.template

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
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
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

/** One-tap hand-off to Waze or Google Maps (the driver's choice in Settings), or any maps app. */
private fun openNavigation(context: Context, destination: String): Boolean {
    val encoded = Uri.encode(destination)
    val useWaze = context.getSharedPreferences("pickup_prefs", Context.MODE_PRIVATE)
        .getInt("nav_app", 0) == 1
    val google = "google.navigation:q=$encoded"
    val waze = "waze://?q=$encoded&navigate=yes"
    val geo = "geo:0,0?q=$encoded"
    val attempts = if (useWaze) listOf(waze, google, geo) else listOf(google, geo)
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

/** m:ss, with a minus sign once the time has run past zero (e.g. -0:23). */
private fun clockSigned(totalSeconds: Int): String {
    val abs = kotlin.math.abs(totalSeconds)
    val sign = if (totalSeconds < 0) "-" else ""
    return "$sign${abs / 60}:${"%02d".format(abs % 60)}"
}

// After this much waiting, the driver can remind the rider.
private const val REMIND_AFTER_SECONDS = 120

// After this much waiting, the driver can cancel with no penalty. The reminder button goes away.
private const val FREE_CANCEL_SECONDS = 300

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
        colors = ButtonDefaults.buttonColors(containerColor = PuInk, contentColor = PuBg)
    ) {
        Text("➤  $label", fontSize = 15.sp, fontWeight = FontWeight.Bold)
    }
}

/** Big, bold timer shown in the top right corner of the map. */
@Composable
private fun ClockBadge(label: String, value: String, bg: Color, fg: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(bg)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(label, color = fg, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Text(value, color = fg, fontSize = 32.sp, fontWeight = FontWeight.ExtraBold)
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
    // Seconds since the ride was accepted, while the driver is still on the way.
    var driveSeconds by remember { mutableIntStateOf(0) }
    var reminderSent by remember { mutableStateOf(false) }
    var riderOnTheWay by remember { mutableStateOf(false) }
    var showCancel by remember { mutableStateOf(false) }
    var reason by remember { mutableStateOf<String?>(null) }
    var showNoMaps by remember { mutableStateOf(false) }
    var showCall by remember { mutableStateOf(false) }
    var showChat by remember { mutableStateOf(false) }
    var showVerify by remember { mutableStateOf(false) }
    var verifyStage by remember { mutableIntStateOf(0) }
    var showMismatch by remember { mutableStateOf(false) }

    // The phone's back button does nothing here. Cancelling is only via "Cancel ride".
    BackHandler { }

    // Before arriving: the time to pickup counts down, then keeps going below zero.
    // The moment the driver taps "I've arrived": the waiting time counts up.
    LaunchedEffect(arrived) {
        if (arrived) {
            while (true) {
                delay(1000)
                waitSeconds++
            }
        } else {
            while (true) {
                delay(1000)
                driveSeconds++
            }
        }
    }
    val secondsToPickup = request.etaMinutes * 60 - driveSeconds

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
            Spacer(Modifier.height(4.dp))

            MapPlaceholder(Modifier.weight(1f)) {
                if (arrived) {
                    ClockBadge(
                        label = "WAITING",
                        value = clockSigned(waitSeconds),
                        bg = PuAmber,
                        fg = PuAmberInk,
                        modifier = Modifier.align(Alignment.TopEnd).padding(12.dp)
                    )
                } else {
                    val late = secondsToPickup < 0
                    ClockBadge(
                        label = if (late) "LATE" else "ARRIVING IN",
                        value = clockSigned(secondsToPickup),
                        bg = if (late) PuDanger else PuAmber,
                        fg = if (late) Color.White else PuAmberInk,
                        modifier = Modifier.align(Alignment.TopEnd).padding(12.dp)
                    )
                }
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
                // Between 2 and 5 minutes of waiting: remind the rider. After 5 minutes, no button.
                if (waitSeconds in REMIND_AFTER_SECONDS until FREE_CANCEL_SECONDS) {
                    when {
                        riderOnTheWay -> {
                            Text(
                                "${request.riderName}: I'm on my way",
                                color = PuAmber, fontSize = 15.sp, fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(10.dp))
                        }
                        reminderSent -> {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "Reminder sent to ${request.riderName}",
                                    color = PuMuted, fontSize = 14.sp
                                )
                                TextButton(onClick = { riderOnTheWay = true }) {
                                    Text("Demo: rider replies", color = PuAmber, fontSize = 12.sp)
                                }
                            }
                        }
                        else -> {
                            OutlineButton(
                                "Remind ${request.riderName} I'm waiting",
                                { reminderSent = true }
                            )
                            Spacer(Modifier.height(10.dp))
                        }
                    }
                }
                PrimaryButton("Start trip", { verifyStage = 0; showVerify = true })
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
    if (showVerify) {
        val revealed = verifyStage == 1
        AlertDialog(
            onDismissRequest = { },
            confirmButton = {
                if (!revealed) {
                    TextButton(onClick = { verifyStage = 1 }) {
                        Text("Demo: rider taps verify", color = PuAmber)
                    }
                } else {
                    TextButton(onClick = { showVerify = false; onStartTrip() }) {
                        Text("Same code, start trip", color = PuAmber, fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                if (!revealed) {
                    TextButton(onClick = { showVerify = false }) {
                        Text("Cancel", color = PuMuted)
                    }
                } else {
                    TextButton(onClick = { showMismatch = true }) {
                        Text("Different code", color = PuDanger)
                    }
                }
            },
            title = { Text(if (revealed) "Check the code" else "Waiting for ${request.riderName}") },
            text = {
                if (!revealed) {
                    Text(
                        "Ask ${request.riderName} to tap \"I'm with my driver\" in their app. The code appears on both phones only after they tap. " +
                            "Skeleton build: use the demo button to pretend they did."
                    )
                } else {
                    Column {
                        Text("Check that this code matches the code on ${request.riderName}'s phone.")
                        Spacer(Modifier.height(12.dp))
                        Text(
                            request.startCode,
                            color = PuInk, fontSize = 44.sp, fontWeight = FontWeight.Bold
                        )
                    }
                }
            },
            containerColor = PuCard,
            titleContentColor = PuInk,
            textContentColor = PuMuted
        )
    }
    if (showMismatch) {
        AlertDialog(
            onDismissRequest = { },
            confirmButton = {
                TextButton(onClick = { showMismatch = false; showVerify = false; onCancel() }) {
                    Text("Cancel ride", color = PuDanger, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showMismatch = false }) {
                    Text("Go back", color = PuMuted)
                }
            },
            title = { Text("Do not start this trip") },
            text = {
                Text("The codes do not match, so this may not be your rider. Stay safe: cancel the ride or contact support.")
            },
            containerColor = PuCard,
            titleContentColor = PuInk,
            textContentColor = PuMuted
        )
    }
    if (showCall) {
        SkeletonDialog(message = "Calling ${request.riderName} comes in a later batch.", onOk = { showCall = false })
    }
    if (showChat) {
        SkeletonDialog(message = "Chat with ${request.riderName} comes in a later batch.", onOk = { showChat = false })
    }
}

private data class DestinationOption(val name: String, val factor: Double)

// PLACEHOLDER options until real address search exists.
private val destinationOptions = listOf(
    DestinationOption("Maryland Mall", 0.6),
    DestinationOption("Ikeja City Mall", 1.4),
    DestinationOption("Lekki Phase 1", 1.9)
)

@Composable
fun TripInProgressScreen(request: RideRequest, onEndTrip: () -> Unit) {
    val context = LocalContext.current
    var current by remember { mutableStateOf(request) }
    var seconds by remember { mutableIntStateOf(0) }
    var showSos by remember { mutableStateOf(false) }
    var sosSent by remember { mutableStateOf(false) }
    var showChat by remember { mutableStateOf(false) }
    var showEnd by remember { mutableStateOf(false) }
    var showNoMaps by remember { mutableStateOf(false) }
    var showChange by remember { mutableStateOf(false) }
    var selected by remember { mutableIntStateOf(-1) }
    var proposal by remember { mutableStateOf<RideRequest?>(null) }

    // A new drop-off keeps the same rate per km, so the fare follows the distance.
    fun proposalFor(option: DestinationOption): RideRequest {
        val km = ((current.distanceKm * option.factor) * 10).roundToInt() / 10.0
        val rate = current.fare / current.distanceKm
        return current.copy(
            dropoff = option.name,
            distanceKm = km,
            fare = roundTo100((rate * km).toInt())
        )
    }

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
                    Text(naira(current.fare), color = PuInk, fontSize = 36.sp, fontWeight = FontWeight.Bold)
                    Text(
                        "${current.riderName} · ${current.paymentMethod}",
                        color = PuMuted, fontSize = 14.sp
                    )
                }
                // SOS, Chat and Change: tiny, above the map, away from the map controls.
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TinyChip("SOS", PuDanger) { showSos = true }
                    TinyChip("Chat", PuInk) { showChat = true }
                    TinyChip("Change", PuInk) { selected = -1; showChange = true }
                }
            }
            Spacer(Modifier.height(12.dp))

            InfoCard {
                AddressRow("A", Color(0xFF3B6BFF), current.pickup, PuMuted, 14.sp)
                Spacer(Modifier.height(8.dp))
                AddressRow("B", Color(0xFF1FA463), current.dropoff, PuInk, 16.sp, true)
            }
            Spacer(Modifier.height(4.dp))

            MapPlaceholder(Modifier.weight(1f)) {
                NavigateButton(
                    label = "Navigate to drop-off",
                    onClick = { if (!openNavigation(context, current.dropoff)) showNoMaps = true },
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(12.dp)
                )
            }
            Spacer(Modifier.height(12.dp))
            PrimaryButton("End trip", { showEnd = true })
        }
    }

    if (showChange) {
        AlertDialog(
            onDismissRequest = { showChange = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        proposal = proposalFor(destinationOptions[selected])
                        showChange = false
                    },
                    enabled = selected >= 0
                ) {
                    Text("Send to rider", color = if (selected >= 0) PuAmber else PuMuted)
                }
            },
            dismissButton = {
                TextButton(onClick = { showChange = false }) {
                    Text("Cancel", color = PuMuted)
                }
            },
            title = { Text("Rider wants a different drop-off?") },
            text = {
                Column {
                    Text(
                        "The fare is recalculated at the same rate per km, and both of you must accept. Skeleton build: real address search comes later.",
                        color = PuMuted, fontSize = 12.sp
                    )
                    Spacer(Modifier.height(8.dp))
                    destinationOptions.forEachIndexed { index, option ->
                        val offer = proposalFor(option)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selected = index }
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selected == index,
                                onClick = null,
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = PuAmber,
                                    unselectedColor = PuMuted
                                )
                            )
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text(option.name, color = PuInk, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                Text(
                                    "${"%.1f".format(offer.distanceKm)} km · ${naira(offer.fare)}",
                                    color = PuMuted, fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            },
            containerColor = PuCard,
            titleContentColor = PuInk,
            textContentColor = PuInk
        )
    }
    proposal?.let { offer ->
        AlertDialog(
            onDismissRequest = { },
            confirmButton = {
                TextButton(onClick = {
                    current = offer
                    DriverRepository.updateTrip(offer)
                    proposal = null
                }) {
                    Text("Rider accepts", color = PuAmber, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { proposal = null }) {
                    Text("Rider declines", color = PuMuted)
                }
            },
            title = { Text("Waiting for ${current.riderName}") },
            text = {
                Text(
                    "New drop-off: ${offer.dropoff}\nNew fare: ${naira(offer.fare)} for ${"%.1f".format(offer.distanceKm)} km\n\n" +
                        "Skeleton build: tap a button to pretend the rider answered. If the rider declines, the trip continues to the original drop-off."
                )
            },
            containerColor = PuCard,
            titleContentColor = PuInk,
            textContentColor = PuMuted
        )
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
            text = { Text("Only end the trip once ${current.riderName} has reached ${current.dropoff}.") },
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
        SkeletonDialog(message = "Chat with ${current.riderName} comes in a later batch.", onOk = { showChat = false })
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

private val riderFlags = listOf("Rude", "Late", "Messy", "Would not pay")

@Composable
fun TripCompleteScreen(request: RideRequest, onDone: () -> Unit) {
    val km = "%.1f".format(request.distanceKm)
    val isCash = request.paymentMethod == "Cash"
    val vat = vatOn(request.fare)
    var stars by remember { mutableIntStateOf(0) }
    var flags by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    // No going back into a finished trip.
    BackHandler { }

    Surface(modifier = Modifier.fillMaxSize(), color = PuBg) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .imePadding()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
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
                Spacer(Modifier.height(4.dp))

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
                Spacer(Modifier.height(4.dp))
                Text("VAT is taken from your wallet, not from your fare.", color = PuMuted, fontSize = 12.sp)
                Spacer(Modifier.height(16.dp))

                // Optional and quick: one tap on the stars is enough.
                InfoCard {
                    Text("Rate ${request.riderName}", color = PuInk, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(6.dp))
                    Row {
                        for (i in 1..5) {
                            Text(
                                if (i <= stars) "★" else "☆",
                                color = PuAmber,
                                fontSize = 34.sp,
                                modifier = Modifier
                                    .clickable { stars = i }
                                    .padding(end = 8.dp)
                            )
                        }
                    }
                    if (stars in 1..3) {
                        Spacer(Modifier.height(8.dp))
                        Text("What went wrong? (optional)", color = PuMuted, fontSize = 12.sp)
                        Spacer(Modifier.height(6.dp))
                        riderFlags.chunked(2).forEach { rowFlags ->
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                rowFlags.forEach { flag ->
                                    val on = flags.split(",").contains(flag)
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(50))
                                            .background(if (on) PuAmber else PuChip)
                                            .clickable {
                                                flags = if (on) {
                                                    flags.split(",").filter { it.isNotEmpty() && it != flag }.joinToString(",")
                                                } else {
                                                    "$flags$flag,"
                                                }
                                            }
                                            .padding(horizontal = 14.dp, vertical = 8.dp)
                                    ) {
                                        Text(
                                            flag,
                                            color = if (on) PuAmberInk else PuInk,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = note,
                        onValueChange = { if (it.length <= 200) note = it },
                        placeholder = {
                            Text("Add a comment or report a problem (optional)", color = PuMuted, fontSize = 13.sp)
                        },
                        minLines = 2,
                        maxLines = 4,
                        colors = fieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Spacer(Modifier.height(4.dp))
            }

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
