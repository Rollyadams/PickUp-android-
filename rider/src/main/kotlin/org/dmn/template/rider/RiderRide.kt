package org.dmn.template.rider

import android.content.ActivityNotFoundException
import android.content.Intent
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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

private const val FARE_STEP = 100

@Composable
private fun StepButton(label: String, enabled: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(PuChip)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            color = if (enabled) PuInk else PuMuted,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

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

/** The fare: recommended price, adjustable inside a range, plus Quick Accept. */
@Composable
fun RiderFareScreen(
    draft: RideDraft,
    onBack: () -> Unit,
    onFind: () -> Unit,
    onQuickAccept: () -> Unit
) {
    val recommended = remember { RiderDemo.recommendedFare(draft.km) }
    val minFare = RiderDemo.minFare(recommended)
    val maxFare = RiderDemo.maxFare(recommended)
    val quickFare = RiderDemo.quickFare(recommended)
    var fare by remember { mutableIntStateOf(recommended) }
    var showOptions by remember { mutableStateOf(false) }
    var showPayment by remember { mutableStateOf(false) }

    val minutes = (draft.km * 3).toInt().coerceAtLeast(5)
    val optionsSummary = listOfNotNull(
        if (draft.childSeat) "Child seat" else null,
        if (draft.extraPassengers) "5+ passengers" else null,
        if (draft.comment.isNotBlank()) "Comment" else null
    ).joinToString(", ").ifEmpty { "None" }

    Surface(modifier = Modifier.fillMaxSize(), color = PuBg) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            TopBar("Your fare", onBack)
            PlaceLine("A", RouteBlue, "Pickup", draft.pickup.name)
            PlaceLine("B", PuGood, "Destination", draft.dropoff?.name ?: "")
            Text(
                "%.1f km · about %d min".format(draft.km, minutes),
                color = PuMuted,
                fontSize = 13.sp
            )
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StepButton("−", fare - FARE_STEP >= minFare) { fare -= FARE_STEP }
                Text(naira(fare), color = PuInk, fontSize = 40.sp, fontWeight = FontWeight.ExtraBold)
                StepButton("+", fare + FARE_STEP <= maxFare) { fare += FARE_STEP }
            }
            Text(
                "Recommended ${naira(recommended)} · range ${naira(minFare)} to ${naira(maxFare)}",
                color = PuMuted,
                fontSize = 13.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(Modifier.height(12.dp))
            ValueRow("Options", optionsSummary) { showOptions = true }
            ValueRow("Payment", draft.payment) { showPayment = true }
            Spacer(Modifier.height(16.dp))
            PrimaryButton("Find a driver · ${naira(fare)}", {
                draft.fare = fare
                onFind()
            })
            Spacer(Modifier.height(10.dp))
            OutlineButton("Quick Accept · ${naira(quickFare)}", {
                draft.fare = quickFare
                onQuickAccept()
            })
            Text(
                "Quick Accept skips the offers: a higher fixed price and a driver is assigned straight away.",
                color = PuMuted,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 6.dp, bottom = 16.dp)
            )
        }
    }

    if (showOptions) OptionsDialog(draft) { showOptions = false }
    if (showPayment) PaymentDialog(draft) { showPayment = false }
}

/** Drivers answer the request. The rider picks one offer. */
@Composable
fun RiderFindingScreen(draft: RideDraft, onAccept: (DriverOffer) -> Unit, onCancel: () -> Unit) {
    val offers = remember { RiderDemo.offersFor(draft.fare) }
    var shown by remember { mutableIntStateOf(0) }

    // Demo: offers arrive one by one.
    LaunchedEffect(Unit) {
        repeat(offers.size) {
            delay(1500)
            shown++
        }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = PuBg) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(16.dp))
            Text("Finding drivers", color = PuInk, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text(
                "Your fare ${naira(draft.fare)}. Drivers nearby can accept or ask for a different price.",
                color = PuMuted,
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
            )
            if (shown < offers.size) {
                Row(
                    modifier = Modifier.padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        color = PuAmber,
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(Modifier.width(10.dp))
                    Text("Waiting for offers", color = PuMuted, fontSize = 14.sp)
                }
            }
            Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                offers.take(shown).forEach { offer ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onAccept(offer) }
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RiderAvatar(offer.name, 44.dp)
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "${offer.name} · ★ ${offer.rating}",
                                    color = PuInk,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "${offer.car} · ${offer.etaMinutes} min away",
                                    color = PuMuted,
                                    fontSize = 13.sp
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    naira(offer.fare),
                                    color = PuInk,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Pill("Accept", PuAmber, PuAmberInk)
                            }
                        }
                        HairLine()
                    }
                }
            }
            OutlineButton("Cancel request", onCancel, color = PuDanger)
            Spacer(Modifier.height(12.dp))
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
        "from ${draft.pickup.name} to ${draft.dropoff?.name ?: ""}."

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
                        draft.dropoff?.name ?: "",
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
                MoneyRow("From", draft.pickup.name)
                MoneyRow("To", draft.dropoff?.name ?: "")
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
