package org.dmn.template.admin

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.dmn.template.PuAmber
import org.dmn.template.PuBg
import org.dmn.template.PuCard
import org.dmn.template.PuDanger
import org.dmn.template.PuInk
import org.dmn.template.PuMuted
import org.dmn.template.fieldColors

@Composable
private fun SettingRow(setting: PriceSetting, onClick: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(
            modifier = Modifier.padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(setting.label, color = PuInk, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text(setting.hint, color = PuMuted, fontSize = 12.sp)
            }
            Spacer(Modifier.width(12.dp))
            Text(
                formatPrice(setting.unit, setting.value),
                color = PuInk,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.width(8.dp))
            Text("›", color = PuMuted, fontSize = 20.sp)
        }
        HairLine()
    }
}

@Composable
private fun EditDialog(setting: PriceSetting, onClose: () -> Unit) {
    var text by remember {
        mutableStateOf(
            if (setting.value == setting.value.toInt().toDouble()) {
                setting.value.toInt().toString()
            } else {
                setting.value.toString()
            }
        )
    }
    val number = text.replace(',', '.').toDoubleOrNull()
    val valid = number != null && number >= setting.min && number <= setting.max
    val message = when {
        number == null -> "Enter a number."
        !valid -> "Must be between ${formatPrice(setting.unit, setting.min)} and " +
            formatPrice(setting.unit, setting.max) + "."
        else -> setting.hint
    }

    AlertDialog(
        onDismissRequest = onClose,
        confirmButton = {
            TextButton(
                enabled = valid,
                onClick = {
                    if (number != null && valid) {
                        AdminRepository.updatePrice(setting.key, number)
                        onClose()
                    }
                }
            ) {
                Text("Save", color = if (valid) PuAmber else PuMuted)
            }
        },
        dismissButton = {
            TextButton(onClick = onClose) { Text("Cancel", color = PuMuted) }
        },
        title = { Text("${setting.label} (${setting.unit})") },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { input ->
                    if (input.length <= 8 && input.all { it.isDigit() || it == '.' }) text = input
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                supportingText = {
                    Text(message, color = if (number == null || !valid) PuDanger else PuMuted)
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

/** Step 3: the numbers behind fares and fees, all editable. Nothing is hardcoded in the apps later. */
@Composable
fun AdminPricingScreen(onBack: () -> Unit) {
    var editing by remember { mutableStateOf<PriceSetting?>(null) }
    val settings = AdminRepository.pricing.toList()
    val changes = AdminRepository.priceChanges.toList()

    Surface(modifier = Modifier.fillMaxSize(), color = PuBg) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            AdminTopBar("Pricing control", onBack)
            Text(
                "Tap a value to change it. For now this only changes the demo; the apps will " +
                    "read these once the backend is connected.",
                color = PuMuted,
                fontSize = 12.sp
            )
            pricingGroups.forEach { group ->
                Text(
                    group,
                    color = PuMuted,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
                )
                HairLine()
                settings.filter { it.group == group }.forEach { setting ->
                    SettingRow(setting) { editing = setting }
                }
            }
            if (changes.isNotEmpty()) {
                Text(
                    "Changes this session",
                    color = PuMuted,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 20.dp, bottom = 4.dp)
                )
                HairLine()
                changes.forEach { line ->
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
            Spacer(Modifier.padding(bottom = 16.dp))
        }
    }

    editing?.let { setting -> EditDialog(setting) { editing = null } }
}
