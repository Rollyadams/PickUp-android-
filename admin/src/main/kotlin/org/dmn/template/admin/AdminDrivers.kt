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
import org.dmn.template.OutlineButton
import org.dmn.template.Pill
import org.dmn.template.PrimaryButton
import org.dmn.template.PuAmber
import org.dmn.template.PuBg
import org.dmn.template.PuCard
import org.dmn.template.PuDanger
import org.dmn.template.PuGood
import org.dmn.template.PuInk
import org.dmn.template.PuMuted
import org.dmn.template.RiderAvatar

/** Step 1: drivers waiting to be approved, newest first. */
@Composable
fun AdminDriversScreen(onBack: () -> Unit, onOpen: (String) -> Unit) {
    val pending = AdminRepository.pending.toList()
    val decisions = AdminRepository.decisions.toList()

    Surface(modifier = Modifier.fillMaxSize(), color = PuBg) {
        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
            AdminTopBar("Driver verification", onBack)
            Text("${pending.size} waiting for approval", color = PuMuted, fontSize = 13.sp)
            Spacer(Modifier.height(8.dp))
            if (pending.isEmpty()) {
                Text(
                    "No drivers waiting.",
                    color = PuMuted,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(pending, key = { it.id }) { app ->
                    val allOk = app.ninOk && app.licenceOk && app.papersOk
                    Column(modifier = Modifier.fillMaxWidth().clickable { onOpen(app.id) }) {
                        Row(
                            modifier = Modifier.padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RiderAvatar(app.name, 44.dp)
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    app.name,
                                    color = PuInk,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    "${app.car} · ${app.plate}",
                                    color = PuMuted,
                                    fontSize = 13.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text("Submitted ${app.submitted}", color = PuMuted, fontSize = 12.sp)
                            }
                            Spacer(Modifier.width(8.dp))
                            if (allOk) {
                                Pill("Checks passed", Color(0x331FA463), PuGood, 11.sp)
                            } else {
                                Pill("Needs review", Color(0x33FF8A1E), PuAmber, 11.sp)
                            }
                        }
                        HairLine()
                    }
                }
                if (decisions.isNotEmpty()) {
                    item {
                        Text(
                            "Decisions this session",
                            color = PuMuted,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
                        )
                    }
                    items(decisions) { line ->
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                line,
                                color = PuInk,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(vertical = 10.dp)
                            )
                            HairLine()
                        }
                    }
                }
            }
        }
    }
}

/** One application: the vendor's check results, then Approve or Reject. */
@Composable
fun AdminDriverDetailScreen(id: String, onBack: () -> Unit) {
    val app = AdminRepository.application(id)
    var rejecting by remember { mutableStateOf(false) }
    if (app == null) {
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
            AdminTopBar("Driver details", onBack)
            Row(
                modifier = Modifier.padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RiderAvatar(app.name, 56.dp)
                Spacer(Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(app.name, color = PuInk, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text(app.phone, color = PuMuted, fontSize = 14.sp)
                }
            }
            InfoCard {
                MoneyRow("Vehicle", app.car)
                MoneyRow("Plate number", app.plate)
                MoneyRow("Submitted", app.submitted)
            }
            Text(
                "Checks from the verification vendor (demo)",
                color = PuMuted,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
            InfoCard {
                MoneyRow(
                    "NIN",
                    if (app.ninOk) "Verified" else "Not matched",
                    valueColor = if (app.ninOk) PuGood else PuDanger
                )
                MoneyRow(
                    "Driver's licence",
                    if (app.licenceOk) "Verified" else "Not matched",
                    valueColor = if (app.licenceOk) PuGood else PuDanger
                )
                MoneyRow(
                    "Vehicle papers",
                    if (app.papersOk) "Uploaded" else "Missing",
                    valueColor = if (app.papersOk) PuGood else PuDanger
                )
            }
            Spacer(Modifier.height(20.dp))
            PrimaryButton("Approve driver", {
                AdminRepository.approve(app)
                onBack()
            })
            Spacer(Modifier.height(10.dp))
            OutlineButton("Reject", { rejecting = true }, color = PuDanger)
            Spacer(Modifier.height(16.dp))
        }
    }

    if (rejecting) {
        AlertDialog(
            onDismissRequest = { rejecting = false },
            confirmButton = {
                TextButton(onClick = { rejecting = false }) { Text("Cancel", color = PuMuted) }
            },
            title = { Text("Reason for rejecting") },
            text = {
                Column {
                    rejectionReasons.forEach { reason ->
                        Text(
                            reason,
                            color = PuInk,
                            fontSize = 16.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    AdminRepository.reject(app, reason)
                                    rejecting = false
                                    onBack()
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
