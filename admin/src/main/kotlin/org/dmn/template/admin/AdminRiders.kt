package org.dmn.template.admin

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.dmn.template.InfoCard
import org.dmn.template.MoneyRow
import org.dmn.template.Pill
import org.dmn.template.PuAmber
import org.dmn.template.PuBg
import org.dmn.template.PuDanger
import org.dmn.template.PuGood
import org.dmn.template.PuInk
import org.dmn.template.PuMuted
import org.dmn.template.RiderAvatar
import org.dmn.template.SegmentedToggle
import org.dmn.template.fieldColors

private val riderFilters = listOf("All riders", "Phone not verified")

/** Step 2: rider accounts. Search by name or phone, or show only riders whose phone is not verified. */
@Composable
fun AdminRidersScreen(onBack: () -> Unit, onOpen: (String) -> Unit) {
    var query by remember { mutableStateOf("") }
    var filter by remember { mutableIntStateOf(0) }

    val riders = AdminRepository.riders.filter { rider ->
        val text = query.trim()
        val matchesText = text.isEmpty() ||
            rider.name.contains(text, ignoreCase = true) ||
            rider.phone.replace(" ", "").contains(text.replace(" ", ""))
        val matchesFilter = filter == 0 || !rider.phoneVerified
        matchesText && matchesFilter
    }

    Surface(modifier = Modifier.fillMaxSize(), color = PuBg) {
        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
            AdminTopBar("Riders", onBack)
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("Search by name or phone", color = PuMuted) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = fieldColors()
            )
            Spacer(Modifier.height(8.dp))
            SegmentedToggle(riderFilters, filter) { filter = it }
            Spacer(Modifier.height(4.dp))
            Text("${riders.size} riders", color = PuMuted, fontSize = 13.sp, modifier = Modifier.padding(vertical = 4.dp))
            if (riders.isEmpty()) {
                Text(
                    "No riders match.",
                    color = PuMuted,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(riders, key = { it.id }) { rider ->
                    Column(modifier = Modifier.fillMaxWidth().clickable { onOpen(rider.id) }) {
                        Row(
                            modifier = Modifier.padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RiderAvatar(rider.name, 44.dp)
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    rider.name,
                                    color = PuInk,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(rider.phone, color = PuMuted, fontSize = 13.sp)
                            }
                            Spacer(Modifier.width(8.dp))
                            if (rider.phoneVerified) {
                                Pill("Verified", Color(0x331FA463), PuGood, 11.sp)
                            } else {
                                Pill("Not verified", Color(0x33FF8A1E), PuAmber, 11.sp)
                            }
                        }
                        HairLine()
                    }
                }
            }
        }
    }
}

/** One rider: account details and whether their phone number is verified. */
@Composable
fun AdminRiderDetailScreen(id: String, onBack: () -> Unit) {
    val rider = AdminRepository.rider(id)
    if (rider == null) {
        Surface(modifier = Modifier.fillMaxSize(), color = PuBg) { }
        return
    }

    Surface(modifier = Modifier.fillMaxSize(), color = PuBg) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            AdminTopBar("Rider details", onBack)
            Row(
                modifier = Modifier.padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RiderAvatar(rider.name, 56.dp)
                Spacer(Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(rider.name, color = PuInk, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text(rider.phone, color = PuMuted, fontSize = 14.sp)
                }
            }
            InfoCard {
                MoneyRow("Joined", rider.joined)
                MoneyRow("Trips", "${rider.trips}")
                MoneyRow("Rating", rider.rating?.let { "★ $it" } ?: "No rating yet")
                MoneyRow("Default payment", rider.payment)
            }
            Text(
                "Verification",
                color = PuMuted,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
            InfoCard {
                MoneyRow(
                    "Phone number",
                    if (rider.phoneVerified) "Verified by code" else "Not verified",
                    valueColor = if (rider.phoneVerified) PuGood else PuDanger
                )
            }
        }
    }
}
