package org.dmn.template

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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun IncomeScreen(onMenu: () -> Unit) {
    var period by remember { mutableIntStateOf(0) }

    val data = DriverRepository.income(period)
    val flatFee = FLAT_FEE * data.daysWorked
    val vat = vatOn(data.fares)
    val net = data.fares - flatFee - vat
    val perKmRate = if (data.km > 0) data.fares / data.km else 0
    val trips = DriverRepository.trips().filter { period != 0 || it.day == "Today" }

    Surface(modifier = Modifier.fillMaxSize(), color = PuBg) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                MenuButton(onClick = onMenu)
                Spacer(Modifier.width(12.dp))
                Text("Income", color = PuInk, fontSize = 26.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(14.dp))
            SegmentedToggle(listOf("Day", "Week", "Month"), period) { period = it }
            Spacer(Modifier.height(18.dp))

            Text("Net income ${data.label}", color = PuMuted, fontSize = 14.sp)
            Text(naira(net), color = PuInk, fontSize = 40.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(14.dp))

            InfoCard {
                MoneyRow("Fares (${data.orders} orders)", naira(data.fares))
                MoneyRow("Commission", naira(0))
                MoneyRow("Flat fee (${data.daysWorked} ${if (data.daysWorked == 1) "day" else "days"})", "−" + naira(flatFee))
                MoneyRow("VAT (7.5%)", "−" + naira(vat))
                Spacer(Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(PuLine)
                )
                Spacer(Modifier.height(6.dp))
                MoneyRow("Net income", naira(net), strong = true)
            }
            Spacer(Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatTile("${data.orders}", "Orders", Modifier.weight(1f))
                StatTile("${data.km} km", "Mileage", Modifier.weight(1f))
                StatTile(naira(perKmRate), "Per km", Modifier.weight(1f))
            }
            Spacer(Modifier.height(20.dp))

            Text("Trips", color = PuInk, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            TripHistoryList(trips)
            Spacer(Modifier.height(8.dp))
        }
    }
}

/** Trips grouped by day. Tap a trip to see its money breakdown. */
@Composable
fun TripHistoryList(trips: List<TripRecord>) {
    var open by remember(trips.size) { mutableStateOf(-1) }
    var lastDay = ""

    trips.forEachIndexed { index, trip ->
        if (trip.day != lastDay) {
            lastDay = trip.day
            Spacer(Modifier.height(8.dp))
            Text(trip.day, color = PuMuted, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
        }
        val expanded = open == index
        InfoCard {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { open = if (expanded) -1 else index }
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(trip.pickup, color = PuInk, fontSize = 14.sp)
                        Text("→ ${trip.dropoff}", color = PuInk, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.width(8.dp))
                    if (trip.status == "Completed") {
                        Text(naira(trip.fare), color = PuInk, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                    } else {
                        Pill(trip.status, PuDanger, Color.White, 11.sp)
                    }
                }
                if (expanded && trip.status == "Completed") {
                    Spacer(Modifier.height(8.dp))
                    MoneyRow("Distance", "${"%.1f".format(trip.km)} km")
                    MoneyRow("Paid by", trip.payment)
                    MoneyRow("VAT (7.5%)", "−" + naira(vatOn(trip.fare)))
                    MoneyRow("You received", naira(trip.fare), strong = true)
                }
            }
        }
        Spacer(Modifier.height(10.dp))
    }
}

@Composable
fun WalletScreen(onMenu: () -> Unit) {
    var showTopUp by remember { mutableStateOf(false) }
    var showAccount by remember { mutableStateOf(false) }

    val today = DriverRepository.income(0)
    val account = DriverRepository.payoutAccount()
    val entries = remember { DriverRepository.walletEntries() }

    Surface(modifier = Modifier.fillMaxSize(), color = PuBg) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                MenuButton(onClick = onMenu)
                Spacer(Modifier.width(12.dp))
                Text("Wallet", color = PuInk, fontSize = 26.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(14.dp))

            InfoCard {
                Text("Prepaid balance", color = PuMuted, fontSize = 14.sp)
                Text(
                    naira(DriverRepository.walletBalance()),
                    color = PuInk, fontSize = 40.sp, fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Your flat fee and VAT are taken from this balance. Days you don't work cost nothing.",
                    color = PuMuted, fontSize = 13.sp
                )
                Spacer(Modifier.height(14.dp))
                PrimaryButton("Top up wallet", { showTopUp = true })
            }
            Spacer(Modifier.height(12.dp))

            InfoCard {
                Text("Taken today", color = PuInk, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                MoneyRow("Flat fee", "−" + naira(FLAT_FEE))
                MoneyRow("VAT collected", "−" + naira(vatOn(today.fares)))
            }
            Spacer(Modifier.height(12.dp))

            InfoCard {
                Text("Where riders pay you", color = PuInk, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(6.dp))
                Text(account.bank, color = PuMuted, fontSize = 13.sp)
                Text(account.number, color = PuInk, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text(account.name, color = PuMuted, fontSize = 13.sp)
                Spacer(Modifier.height(4.dp))
                Text(
                    "Riders who pay by transfer see this account.",
                    color = PuMuted, fontSize = 12.sp
                )
                Spacer(Modifier.height(12.dp))
                OutlineButton("Change account", { showAccount = true })
            }
            Spacer(Modifier.height(16.dp))

            Text("Recent activity", color = PuInk, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            entries.forEach { entry ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(entry.title, color = PuInk, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        Text(entry.note, color = PuMuted, fontSize = 12.sp)
                    }
                    val credit = entry.amount > 0
                    Text(
                        (if (credit) "+" else "−") + naira(kotlin.math.abs(entry.amount)),
                        color = if (credit) PuGood else PuInk,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(PuChip)
                )
            }
            Spacer(Modifier.height(8.dp))
        }
    }

    if (showTopUp) {
        SkeletonDialog(message = "Topping up by bank transfer comes with the wallet build.", onOk = { showTopUp = false })
    }
    if (showAccount) {
        SkeletonDialog(message = "Changing your bank account comes in a later batch.", onOk = { showAccount = false })
    }
}
