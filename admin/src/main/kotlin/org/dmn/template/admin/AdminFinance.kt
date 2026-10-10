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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.dmn.template.InfoCard
import org.dmn.template.MoneyRow
import org.dmn.template.OutlineButton
import org.dmn.template.Pill
import org.dmn.template.PuAmber
import org.dmn.template.PuBg
import org.dmn.template.PuCard
import org.dmn.template.PuDanger
import org.dmn.template.PuGood
import org.dmn.template.PuInk
import org.dmn.template.PuMuted
import org.dmn.template.RiderAvatar
import org.dmn.template.SegmentedToggle
import org.dmn.template.fieldColors
import org.dmn.template.naira

@Composable
private fun SectionLabel(text: String) {
    Text(
        text,
        color = PuMuted,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 18.dp, bottom = 4.dp)
    )
}

@Composable
private fun RemitDialog(owed: Int, onClose: () -> Unit) {
    var text by remember { mutableStateOf("") }
    val amount = text.toIntOrNull()
    val valid = amount != null && amount > 0 && amount <= owed

    AlertDialog(
        onDismissRequest = onClose,
        confirmButton = {
            TextButton(
                enabled = valid,
                onClick = {
                    if (amount != null && valid) {
                        AdminRepository.recordRemittance(amount)
                        onClose()
                    }
                }
            ) {
                Text("Save", color = if (valid) PuAmber else PuMuted)
            }
        },
        dismissButton = { TextButton(onClick = onClose) { Text("Cancel", color = PuMuted) } },
        title = { Text("Record VAT remitted") },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { input ->
                    if (input.length <= 9 && input.all { it.isDigit() }) text = input
                },
                placeholder = { Text("Amount in ₦", color = PuMuted) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                supportingText = {
                    Text(
                        if (amount != null && amount > owed) {
                            "More than the ${naira(owed)} still to remit."
                        } else {
                            "Still to remit: ${naira(owed)}"
                        },
                        color = if (amount != null && amount > owed) PuDanger else PuMuted
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                colors = fieldColors()
            )
        },
        containerColor = PuCard,
        titleContentColor = PuInk,
        textContentColor = PuInk
    )
}

/** Step 5: what came in, the VAT held for the government, and each driver's wallet. */
@Composable
fun AdminFinanceScreen(onBack: () -> Unit, onOpenWallet: (String) -> Unit) {
    var period by remember { mutableIntStateOf(0) }
    var remitting by remember { mutableStateOf(false) }
    val totals = if (period == 0) AdminRepository.totalsToday else AdminRepository.totalsMonth
    val owed = AdminRepository.vatOwed()
    val dailyFee = AdminRepository.dailyFee()
    val remittances = AdminRepository.remittances.toList()

    Surface(modifier = Modifier.fillMaxSize(), color = PuBg) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            AdminTopBar("Finance", onBack)
            SegmentedToggle(listOf("Today", "This month"), period) { period = it }
            Spacer(Modifier.height(8.dp))
            InfoCard {
                MoneyRow("Trips", "%,d".format(totals.trips))
                MoneyRow("Daily fees collected", naira(totals.feesCollected), strong = true)
                MoneyRow("VAT collected", naira(totals.vatCollected))
            }
            Text(
                "Fares of ${naira(totals.faresPaid)} were paid straight to drivers. They are not Pick Up's income.",
                color = PuMuted,
                fontSize = 12.sp
            )

            SectionLabel("VAT position, all time")
            InfoCard {
                MoneyRow("Collected", naira(AdminRepository.VAT_COLLECTED_TO_DATE))
                MoneyRow("Remitted", naira(AdminRepository.vatRemitted()))
                MoneyRow(
                    "Still to remit",
                    naira(owed),
                    strong = true,
                    valueColor = if (owed > 0) PuDanger else PuGood
                )
            }
            Spacer(Modifier.height(8.dp))
            OutlineButton("Record VAT remitted", { remitting = true })
            remittances.forEach { record ->
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(record.date, color = PuMuted, fontSize = 14.sp, modifier = Modifier.weight(1f))
                        Text(naira(record.amount), color = PuInk, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }
                    HairLine()
                }
            }

            SectionLabel("Driver wallets")
            HairLine()
            AdminRepository.wallets.forEach { wallet ->
                Column(modifier = Modifier.fillMaxWidth().clickable { onOpenWallet(wallet.id) }) {
                    Row(
                        modifier = Modifier.padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RiderAvatar(wallet.name, 40.dp)
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(wallet.name, color = PuInk, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Text("Balance ${naira(wallet.balance)}", color = PuMuted, fontSize = 13.sp)
                        }
                        if (wallet.balance < dailyFee) {
                            Pill("Low", Color(0x33E5484D), PuDanger, 11.sp)
                            Spacer(Modifier.width(8.dp))
                        }
                        Text("›", color = PuMuted, fontSize = 20.sp)
                    }
                    HairLine()
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }

    if (remitting) RemitDialog(owed) { remitting = false }
}

/** One driver's wallet and every fee, VAT charge and top-up in it. */
@Composable
fun AdminWalletScreen(id: String, onBack: () -> Unit) {
    val wallet = AdminRepository.wallet(id)
    if (wallet == null) {
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
            AdminTopBar(wallet.name, onBack)
            Text("Balance", color = PuMuted, fontSize = 13.sp)
            Text(naira(wallet.balance), color = PuInk, fontSize = 34.sp, fontWeight = FontWeight.ExtraBold)
            SectionLabel("Fees, VAT and top-ups")
            HairLine()
            wallet.entries.forEach { entry ->
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(entry.label, color = PuInk, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            Text(entry.date, color = PuMuted, fontSize = 12.sp)
                        }
                        Text(
                            (if (entry.amount > 0) "+" else "−") + naira(kotlin.math.abs(entry.amount)),
                            color = if (entry.amount > 0) PuGood else PuInk,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    HairLine()
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}
