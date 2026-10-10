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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import org.dmn.template.PrimaryButton
import org.dmn.template.PuAmber
import org.dmn.template.PuBg
import org.dmn.template.PuCard
import org.dmn.template.PuChip
import org.dmn.template.PuDanger
import org.dmn.template.PuGood
import org.dmn.template.PuInk
import org.dmn.template.PuMuted
import org.dmn.template.SegmentedToggle
import org.dmn.template.naira

/** Step 4: disputes, open ones first. */
@Composable
fun AdminDisputesScreen(onBack: () -> Unit, onOpen: (String) -> Unit) {
    var tab by remember { mutableIntStateOf(0) }
    val all = AdminRepository.disputes.toList()
    val open = all.filter { it.outcome == null }
    val resolved = all.filter { it.outcome != null }
    val shown = if (tab == 0) open else resolved

    Surface(modifier = Modifier.fillMaxSize(), color = PuBg) {
        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
            AdminTopBar("Disputes", onBack)
            SegmentedToggle(listOf("Open (${open.size})", "Resolved (${resolved.size})"), tab) { tab = it }
            Spacer(Modifier.height(8.dp))
            if (shown.isEmpty()) {
                Text(
                    if (tab == 0) "No open disputes." else "Nothing resolved yet.",
                    color = PuMuted,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(shown, key = { it.id }) { dispute ->
                    Column(modifier = Modifier.fillMaxWidth().clickable { onOpen(dispute.id) }) {
                        Row(
                            modifier = Modifier.padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    dispute.title,
                                    color = PuInk,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    "${dispute.riderName} and ${dispute.driverName} · ${naira(dispute.fare)}",
                                    color = PuMuted,
                                    fontSize = 13.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(dispute.opened, color = PuMuted, fontSize = 12.sp)
                            }
                            Spacer(Modifier.width(8.dp))
                            Pill(dispute.payment, PuChip, PuInk, 11.sp)
                        }
                        HairLine()
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text,
        color = PuMuted,
        fontSize = 12.sp,
        modifier = Modifier.padding(top = 12.dp, bottom = 2.dp)
    )
}

/** One dispute: the trip, the payment confirmation, both sides, the chat, and how it ended. */
@Composable
fun AdminDisputeDetailScreen(id: String, onBack: () -> Unit) {
    val dispute = AdminRepository.dispute(id)
    var resolving by remember { mutableStateOf(false) }
    if (dispute == null) {
        Surface(modifier = Modifier.fillMaxSize(), color = PuBg) { }
        return
    }

    val mismatch = dispute.riderMarkedPaid != dispute.driverConfirmedReceived

    Surface(modifier = Modifier.fillMaxSize(), color = PuBg) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            AdminTopBar("Dispute", onBack)
            Text(dispute.title, color = PuInk, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text("Opened ${dispute.opened}", color = PuMuted, fontSize = 13.sp)

            SectionLabel("Trip")
            InfoCard {
                MoneyRow("Route", dispute.route)
                MoneyRow("Fare", naira(dispute.fare))
                MoneyRow("Payment", dispute.payment)
                MoneyRow("Rider", dispute.riderName)
                MoneyRow("Driver", dispute.driverName)
            }

            SectionLabel("Payment confirmation")
            InfoCard {
                MoneyRow(
                    "Rider marked paid",
                    if (dispute.riderMarkedPaid) "Yes" else "No",
                    valueColor = if (dispute.riderMarkedPaid) PuGood else PuMuted
                )
                MoneyRow(
                    "Driver confirmed received",
                    if (dispute.driverConfirmedReceived) "Yes" else "No",
                    valueColor = if (dispute.driverConfirmedReceived) PuGood else PuMuted
                )
            }
            if (mismatch) {
                Text(
                    "The two sides do not agree on payment.",
                    color = PuDanger,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            SectionLabel("Rider's side")
            Text(dispute.riderSide, color = PuInk, fontSize = 15.sp)
            SectionLabel("Driver's side")
            Text(dispute.driverSide, color = PuInk, fontSize = 15.sp)

            SectionLabel("Chat")
            if (dispute.chat.isEmpty()) {
                Text("No messages.", color = PuMuted, fontSize = 14.sp)
            }
            dispute.chat.forEach { line ->
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "${line.from}: ${line.text}",
                        color = PuInk,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    HairLine()
                }
            }

            Spacer(Modifier.height(20.dp))
            if (dispute.outcome == null) {
                PrimaryButton("Resolve dispute", { resolving = true })
            } else {
                Text(
                    "Resolved: ${dispute.outcome}",
                    color = PuGood,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.height(16.dp))
        }
    }

    if (resolving) {
        AlertDialog(
            onDismissRequest = { resolving = false },
            confirmButton = {
                TextButton(onClick = { resolving = false }) { Text("Cancel", color = PuMuted) }
            },
            title = { Text("How is it resolved?") },
            text = {
                Column {
                    disputeOutcomes.forEach { outcome ->
                        Text(
                            outcome,
                            color = PuAmber,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    AdminRepository.resolve(dispute.id, outcome)
                                    resolving = false
                                }
                                .padding(vertical = 12.dp)
                        )
                    }
                }
            },
            containerColor = PuCard,
            titleContentColor = PuInk,
            textContentColor = PuInk
        )
    }
}
