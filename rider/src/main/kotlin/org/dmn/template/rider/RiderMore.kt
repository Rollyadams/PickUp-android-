package org.dmn.template.rider

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.dmn.template.OutlineButton
import org.dmn.template.Pill
import org.dmn.template.PuBg
import org.dmn.template.PuChip
import org.dmn.template.PuDanger
import org.dmn.template.PuInk
import org.dmn.template.PuMuted
import org.dmn.template.RiderAvatar
import org.dmn.template.SegmentedToggle
import org.dmn.template.SkeletonDialog
import org.dmn.template.appVersion
import org.dmn.template.naira

private val dateFilters = listOf("All time", "This week", "This month")
private val paymentFilters = listOf("All", CASH, BANK)

/** Past rides. Filters: date range and payment method (no city/delivery split, cars only). */
@Composable
fun RiderHistoryScreen(onBack: () -> Unit) {
    var dateFilter by remember { mutableIntStateOf(0) }
    var paymentFilter by remember { mutableIntStateOf(0) }

    val trips = RiderDemo.trips.filter { trip ->
        val inRange = when (dateFilter) {
            1 -> trip.daysAgo <= 7
            2 -> trip.daysAgo <= 30
            else -> true
        }
        val paymentOk = paymentFilter == 0 || trip.payment == paymentFilters[paymentFilter]
        inRange && paymentOk
    }

    Surface(modifier = Modifier.fillMaxSize(), color = PuBg) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(horizontal = 20.dp)
        ) {
            TopBar("Trip history", onBack)
            SegmentedToggle(dateFilters, dateFilter) { dateFilter = it }
            Spacer(Modifier.height(8.dp))
            SegmentedToggle(paymentFilters, paymentFilter) { paymentFilter = it }
            Spacer(Modifier.height(8.dp))
            if (trips.isEmpty()) {
                Text(
                    "No trips match these filters.",
                    color = PuMuted,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(trips) { trip ->
                    Column(modifier = Modifier.padding(vertical = 10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(trip.date, color = PuMuted, fontSize = 13.sp)
                            Pill(trip.payment, PuChip, PuInk, 11.sp)
                        }
                        Text(
                            "${trip.pickup} → ${trip.dropoff}",
                            color = PuInk,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Driver ${trip.driver}", color = PuMuted, fontSize = 13.sp)
                            Text(
                                naira(trip.fare),
                                color = PuInk,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    HairLine()
                }
            }
        }
    }
}

/** Profile and the few settings a rider has. */
@Composable
fun RiderProfileScreen(draft: RideDraft, phone: String, onBack: () -> Unit, onLogout: () -> Unit) {
    var dialog by remember { mutableStateOf<String?>(null) }

    Surface(modifier = Modifier.fillMaxSize(), color = PuBg) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(horizontal = 20.dp)
        ) {
            TopBar("Profile and settings", onBack)
            Row(
                modifier = Modifier.padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RiderAvatar("Rider", 56.dp)
                Spacer(Modifier.width(14.dp))
                Column {
                    Text("Rider", color = PuInk, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text(phone.ifEmpty { "No number yet" }, color = PuMuted, fontSize = 14.sp)
                }
            }
            HairLine()
            Spacer(Modifier.height(12.dp))
            Text("Default payment", color = PuMuted, fontSize = 13.sp)
            Spacer(Modifier.height(6.dp))
            SegmentedToggle(listOf(CASH, BANK), if (draft.payment == BANK) 1 else 0) { index ->
                draft.payment = if (index == 1) BANK else CASH
            }
            Spacer(Modifier.height(8.dp))
            ValueRow("Emergency contact", "Add") { dialog = "Emergency contacts come later." }
            ValueRow("Help and support", "") { dialog = "Help and support come later." }
            Spacer(Modifier.weight(1f))
            OutlineButton("Sign out", onLogout, color = PuDanger)
            Spacer(Modifier.height(8.dp))
            Text("Build ${appVersion()}", color = PuMuted, fontSize = 11.sp)
            Spacer(Modifier.height(12.dp))
        }
    }

    dialog?.let { message -> SkeletonDialog(message) { dialog = null } }
}
