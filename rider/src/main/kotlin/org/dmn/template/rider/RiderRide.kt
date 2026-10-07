package org.dmn.template.rider

import android.content.ActivityNotFoundException
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import org.dmn.template.InfoCard
import org.dmn.template.MapPlaceholder
import org.dmn.template.MoneyRow
import org.dmn.template.OutlineButton
import org.dmn.template.Pill
import org.dmn.template.PrimaryButton
import org.dmn.template.PuAmber
import org.dmn.template.PuAmberInk
import org.dmn.template.PuBg
import org.dmn.template.PuCard
import org.dmn.template.PuChip
import org.dmn.template.PuDanger
import org.dmn.template.PuGood
import org.dmn.template.PuInk
import org.dmn.template.PuMuted
import org.dmn.template.RiderAvatar
import org.dmn.template.SkeletonDialog
import org.dmn.template.StopMarker
import org.dmn.template.fieldColors
import org.dmn.template.naira

@Composable
private fun SwitchLine(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onChange(!checked) }
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = PuInk, fontSize = 15.sp, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onChange)
    }
}

@Composable
private fun OptionsDialog(draft: RideDraft, onDone: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDone,
        confirmButton = { TextButton(onClick = onDone) { Text("Done", color = PuAmber) } },
        title = { Text("Options") },
        text = {
            Column {
                SwitchLine("Child safety seat", draft.childSeat) { draft.childSeat = it }
                SwitchLine("More than 4 passengers", draft.extraPassengers) { draft.extraPassengers = it }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = draft.comment,
                    onValueChange = { draft.comment = it },
                    placeholder = { Text("Comment for the driver", color = PuMuted) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = fieldColors()
                )
            }
        },
        containerColor = PuCard,
        titleContentColor = PuInk,
        textContentColor = PuInk
    )
}

@Composable
private fun PaymentDialog(draft: RideDraft, onDone: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDone,
        confirmButton = { TextButton(onClick = onDone) { Text("Close", color = PuMuted) } },
        title = { Text("Payment method") },
        text = {
            Column {
                listOf(CASH, BANK).forEach { method ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                draft.payment = method
                                onDone()
                            }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            if (draft.payment == method) "◉" else "○",
                            color = PuAmber,
                            fontSize = 20.sp
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(method, color = PuInk, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Text(
                    "With bank transfer you get the driver's account details when the trip ends.",
                    color = PuMuted,
                    fontSize = 12.sp
                )
            }
        },
        containerColor = PuCard,
        titleContentColor = PuInk,
        textContentColor = PuInk
    )
}

@Composable
private fun TripRow(
    letter: String,
    color: Color,
    text: String,
    sub: String?,
    onClick: () -> Unit,
    trailing: @Composable () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        StopMarker(letter, color)
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text,
                color = PuInk,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (sub != null) Text(sub, color = PuMuted, fontSize = 13.sp)
        }
        Spacer(Modifier.width(8.dp))
        trailing()
    }
}

@Composable
private fun RideTypeRow(type: RideType, fare: Int, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(if (selected) PuChip else PuCard)
            .border(1.dp, if (selected) PuAmber else PuCard, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(type.glyph, fontSize = 28.sp)
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(type.name, color = PuInk, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text("4 seats · ${type.etaMinutes} min", color = PuMuted, fontSize = 13.sp)
            Text(type.note, color = PuMuted, fontSize = 13.sp)
        }
        Spacer(Modifier.width(8.dp))
        Text(naira(fare), color = PuInk, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
    }
}

/**
 * Trip details over the map, and a sheet of ride types under it. The sheet can be pulled down
 * (or tapped on its handle) so more of the map shows. The fare is set by Pick Up and the rider
 * cannot lower it, so there is no plus or minus here.
 */
@Composable
fun RiderFareScreen(
    draft: RideDraft,
    onBack: () -> Unit,
    onEditPickup: () -> Unit,
    onEditStop: (Int) -> Unit,
    onAddStop: () -> Unit,
    onFind: () -> Unit,
    onQuickAccept: () -> Unit
) {
    val pickup = draft.pickup ?: return
    val km = draft.km
    val recommended = RiderDemo.recommendedFare(km)
    val selected = RiderDemo.rideTypes.firstOrNull { it.id == draft.rideType } ?: RiderDemo.rideTypes[0]
    val fare = maxOf(RiderDemo.fareFor(selected.id, recommended), RiderDemo.minimumFare(km))
    var expanded by remember { mutableStateOf(true) }
    var showOptions by remember { mutableStateOf(false) }
    var showPayment by remember { mutableStateOf(false) }
    val minutes = (km * 3).toInt().coerceAtLeast(5)

    BackHandler { onBack() }

    Surface(modifier = Modifier.fillMaxSize(), color = PuBg) {
        Column(modifier = Modifier.fillMaxSize().systemBarsPadding()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                MapPlaceholder(
                    Modifier
                        .fillMaxSize()
                        .padding(start = 12.dp, end = 12.dp, top = 8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(12.dp)
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(PuCard)
                            .clickable(onClick = onBack),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("←", color = PuInk, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Column(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(PuCard)
                        .padding(horizontal = 14.dp, vertical = 4.dp)
                ) {
                    TripRow("A", RouteBlue, pickup.name, null, onEditPickup) {
                        Pill(
                            if (draft.pickupIsManual) "Pinned spot" else "My location",
                            PuChip,
                            PuInk,
                            11.sp
                        )
                    }
                    draft.stops.forEachIndexed { index, stop ->
                        TripRow(
                            letter = ('B' + index).toString(),
                            color = PuGood,
                            text = stop.name,
                            sub = if (index == 0) "about $minutes min" else null,
                            onClick = { onEditStop(index) }
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (index > 0) {
                                    Text(
                                        "✕",
                                        color = PuMuted,
                                        fontSize = 18.sp,
                                        modifier = Modifier
                                            .clickable { draft.stops.removeAt(index) }
                                            .padding(8.dp)
                                    )
                                }
                                if (index == draft.stops.lastIndex && draft.stops.size < MAX_STOPS) {
                                    Text(
                                        "+",
                                        color = PuInk,
                                        fontSize = 26.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier
                                            .clickable(onClick = onAddStop)
                                            .padding(horizontal = 8.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .background(PuBg)
                    .padding(horizontal = 20.dp)
            ) {
                // Handle: drag it down or up, or tap it.
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .pointerInput(Unit) {
                            var total = 0f
                            detectVerticalDragGestures(
                                onDragStart = { total = 0f },
                                onDragEnd = {
                                    if (total > 40f) {
                                        expanded = false
                                    } else if (total < -40f) {
                                        expanded = true
                                    }
                                },
                                onVerticalDrag = { _, amount -> total += amount }
                            )
                        }
                        .clickable { expanded = !expanded }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .width(40.dp)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(PuMuted)
                    )
                }

                if (expanded) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 280.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        RiderDemo.rideTypes.forEach { type ->
                            RideTypeRow(
                                type,
                                maxOf(RiderDemo.fareFor(type.id, recommended), RiderDemo.minimumFare(km)),
                                draft.rideType == type.id
                            ) { draft.rideType = type.id }
                        }
                    }
                } else {
                    RideTypeRow(selected, fare, true) { expanded = true }
                }
                Text(
                    "Fares are set by Pick Up. Drivers can accept or counter.",
                    color = PuMuted,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 6.dp)
                )

                if (selected.id != 2) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { draft.autoAccept = !draft.autoAccept }
                            .padding(top = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Take the first offer automatically · ${naira(fare)}",
                            color = PuInk,
                            fontSize = 14.sp,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(Modifier.width(8.dp))
                        Switch(checked = draft.autoAccept, onCheckedChange = { draft.autoAccept = it })
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconBox(if (draft.payment == BANK) "🏦" else "💵") { showPayment = true }
                    AmberButton(
                        text = if (selected.id == 2) "Quick Accept · ${naira(fare)}" else "Find drivers · ${naira(fare)}",
                        onClick = {
                            draft.fare = fare
                            if (selected.id == 2) onQuickAccept() else onFind()
                        },
                        modifier = Modifier.weight(1f)
                    )
                    IconBox("⚙") { showOptions = true }
                }
            }
        }
    }

    if (showOptions) OptionsDialog(draft) { showOptions = false }
    if (showPayment) PaymentDialog(draft) { showPayment = false }
}

@Composable
private fun OfferRow(offer: DriverOffer, yourFare: Boolean, onDecline: () -> Unit, onAccept: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp)) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(naira(offer.fare), color = PuInk, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
            Spacer(Modifier.width(10.dp))
            Text("${offer.etaMinutes} min", color = PuMuted, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
        if (yourFare) {
            Spacer(Modifier.height(4.dp))
            Pill("Your fare", Color(0x331FA463), PuGood, 12.sp)
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RiderAvatar(offer.name, 44.dp)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "${offer.name} · ★ ${offer.rating}",
                    color = PuInk,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    "${offer.trips} rides · ${offer.car}",
                    color = PuMuted,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlineButton("Decline", onDecline, Modifier.weight(1f))
            AmberButton("Accept", onAccept, Modifier.weight(1f))
        }
    }
    HairLine()
}

/** Waiting for drivers, then the offers. The rider chooses; nothing is assigned without a choice. */
@Composable
fun RiderFindingScreen(draft: RideDraft, onAccept: (DriverOffer) -> Unit, onCancel: () -> Unit) {
    val offers = remember { mutableStateListOf<DriverOffer>() }
    var secondsLeft by remember { mutableIntStateOf(60) }
    val viewing = 11

    BackHandler { }

    // Demo: offers arrive one by one.
    LaunchedEffect(Unit) {
        for (offer in RiderDemo.offersFor(draft.fare)) {
            delay(2500)
            offers.add(offer)
            if (draft.autoAccept && offers.size == 1) {
                onAccept(offer)
                return@LaunchedEffect
            }
        }
    }
    LaunchedEffect(Unit) {
        while (secondsLeft > 0) {
            delay(1000)
            secondsLeft--
        }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = PuBg) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(horizontal = 20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "✕  Cancel request",
                    color = PuDanger,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(Color(0x22E5484D))
                        .clickable(onClick = onCancel)
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                )
                Spacer(Modifier.weight(1f))
                Text("✔ Verified drivers", color = PuGood, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    if (offers.isEmpty()) "Waiting for responses" else "${offers.size} offers",
                    color = PuInk,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Text(mmss(secondsLeft), color = PuInk, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            }
            Text("You choose your driver", color = PuMuted, fontSize = 14.sp)
            Spacer(Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(PuChip)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth((secondsLeft / 60f).coerceIn(0f, 1f))
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(PuInk)
                )
            }
            Spacer(Modifier.height(12.dp))

            if (offers.isEmpty()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    listOf("Emeka", "Sade", "Ibrahim", "Tunde").forEach { name ->
                        RiderAvatar(name, 30.dp)
                        Spacer(Modifier.width(4.dp))
                    }
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "$viewing drivers are viewing your request",
                        color = PuInk,
                        fontSize = 14.sp,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(Modifier.height(12.dp))
                MapPlaceholder(Modifier.weight(1f))
            } else {
                Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                    offers.toList().forEach { offer ->
                        OfferRow(
                            offer = offer,
                            yourFare = offer.fare == draft.fare,
                            onDecline = { offers.remove(offer) },
                            onAccept = { onAccept(offer) }
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { draft.autoAccept = !draft.autoAccept }
                    .padding(top = 10.dp, bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Your fare ${naira(draft.fare)}. Take the first offer automatically",
                    color = PuInk,
                    fontSize = 14.sp,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                Switch(checked = draft.autoAccept, onCheckedChange = { draft.autoAccept = it })
            }
        }
    }
}

@Composable
private fun DriverLine(offer: DriverOffer) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        RiderAvatar(offer.name, 52.dp)
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(offer.name, color = PuInk, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text("★ ${offer.rating} (${offer.trips} trips)", color = PuMuted, fontSize = 13.sp)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(offer.car, color = PuInk, fontSize = 13.sp)
            Text(offer.plate, color = PuMuted, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
    }
}

/** A driver accepted: who is coming, how far, call or chat. */
@Composable
fun RiderMatchedScreen(draft: RideDraft, onStartTrip: () -> Unit, onCancel: () -> Unit) {
    val offer = draft.driver ?: return
    var dialog by remember { mutableStateOf<String?>(null) }
    BackHandler { }

    Surface(modifier = Modifier.fillMaxSize(), color = PuBg) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(horizontal = 20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Your driver is coming",
                    color = PuInk,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Pill("${offer.etaMinutes} min away", PuChip, PuInk, 13.sp)
            }
            Spacer(Modifier.height(8.dp))
            InfoCard {
                DriverLine(offer)
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Pill(draft.payment, PuChip, PuInk, 11.sp)
                    Spacer(Modifier.width(10.dp))
                    Text(naira(draft.fare), color = PuInk, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlineButton("Call", { dialog = "Calling the driver comes later." }, Modifier.weight(1f))
                OutlineButton("Chat", { dialog = "Chat comes later." }, Modifier.weight(1f))
            }
            Spacer(Modifier.height(8.dp))
            MapPlaceholder(Modifier.weight(1f))
            Spacer(Modifier.height(8.dp))
            PrimaryButton("Demo: driver arrived, start trip", onStartTrip)
            Spacer(Modifier.height(8.dp))
            OutlineButton("Cancel ride", onCancel, color = PuDanger)
            Spacer(Modifier.height(12.dp))
        }
    }

    dialog?.let { message -> SkeletonDialog(message) { dialog = null } }
}

/** Live trip: map, SOS, and sharing the trip on WhatsApp. */
@Composable
fun RiderTripScreen(draft: RideDraft, onArrived: () -> Unit) {
    val offer = draft.driver ?: return
    val context = LocalContext.current
    var showSos by remember { mutableStateOf(false) }
    BackHandler { }

    val shareText = "I'm on a Pick Up ride with ${offer.name} (${offer.car}, ${offer.plate}) " +
        "from ${(draft.pickup?.name ?: "")} to ${draft.destination?.name ?: ""}."

    Surface(modifier = Modifier.fillMaxSize(), color = PuBg) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(horizontal = 20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "On your way",
                    color = PuInk,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Pill("${(draft.km * 3).toInt().coerceAtLeast(5)} min left", PuAmber, PuAmberInk, 13.sp)
            }
            Spacer(Modifier.height(8.dp))
            MapPlaceholder(Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(12.dp)
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(PuDanger)
                        .clickable { showSos = true },
                    contentAlignment = Alignment.Center
                ) {
                    Text("SOS", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                }
            }
            Spacer(Modifier.height(8.dp))
            InfoCard {
                DriverLine(offer)
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StopMarker("B", PuGood)
                    Spacer(Modifier.width(10.dp))
                    Text(
                        draft.destination?.name ?: "",
                        color = PuInk,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            OutlineButton("Share trip on WhatsApp", {
                val send = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, shareText)
                }
                try {
                    context.startActivity(Intent(send).setPackage("com.whatsapp"))
                } catch (e: ActivityNotFoundException) {
                    context.startActivity(Intent.createChooser(send, "Share trip"))
                }
            })
            Spacer(Modifier.height(8.dp))
            PrimaryButton("Demo: arrive at destination", onArrived)
            Spacer(Modifier.height(12.dp))
        }
    }

    if (showSos) SkeletonDialog("SOS would alert Pick Up support and your emergency contact.") {
        showSos = false
    }
}

/** Trip finished: fare summary, and the driver's bank details for bank transfer. */
@Composable
fun RiderCompleteScreen(draft: RideDraft, onDone: () -> Unit) {
    val offer = draft.driver ?: return
    BackHandler { }

    Surface(modifier = Modifier.fillMaxSize(), color = PuBg) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(16.dp))
            Text("You have arrived", color = PuInk, fontSize = 26.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            InfoCard {
                MoneyRow("From", (draft.pickup?.name ?: ""))
                MoneyRow("To", draft.destination?.name ?: "")
                MoneyRow("Distance", "%.1f km".format(draft.km))
                MoneyRow("Payment", draft.payment)
                MoneyRow("Total", naira(draft.fare), strong = true)
            }
            if (draft.payment == BANK) {
                Spacer(Modifier.height(4.dp))
                Text("Pay by bank transfer", color = PuInk, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                InfoCard {
                    // Demo details only. Real details come from the driver's profile.
                    MoneyRow("Bank", "Demo Bank")
                    MoneyRow("Account name", offer.name)
                    MoneyRow("Account number", "0000000000")
                }
            } else {
                Spacer(Modifier.height(8.dp))
                Text(
                    "Pay ${naira(draft.fare)} in cash to ${offer.name}.",
                    color = PuMuted,
                    fontSize = 14.sp
                )
            }
            Spacer(Modifier.height(20.dp))
            PrimaryButton("Done", onDone)
            Spacer(Modifier.height(12.dp))
        }
    }
}

/** Star rating for the driver. */
@Composable
fun RiderRateScreen(draft: RideDraft, onFinish: () -> Unit) {
    val offer = draft.driver
    BackHandler { }

    Surface(modifier = Modifier.fillMaxSize(), color = PuBg) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .imePadding()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(24.dp))
            Text(
                "How was your ride with ${offer?.name ?: "your driver"}?",
                color = PuInk,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                (1..5).forEach { star ->
                    Text(
                        if (star <= draft.rating) "★" else "☆",
                        color = if (star <= draft.rating) PuAmber else PuMuted,
                        fontSize = 44.sp,
                        modifier = Modifier.clickable { draft.rating = star }
                    )
                }
            }
            Spacer(Modifier.height(24.dp))
            PrimaryButton("Submit", onFinish, enabled = draft.rating > 0)
            TextButton(onClick = onFinish) {
                Text("Skip", color = PuMuted)
            }
        }
    }
}
