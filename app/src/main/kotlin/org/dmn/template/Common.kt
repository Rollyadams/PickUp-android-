package org.dmn.template

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Colours that change between light mode and dark mode. */
class PuColors(
    val bg: Color,
    val card: Color,
    val ink: Color,
    val muted: Color,
    val chip: Color,
    val line: Color,
    val map: Color
)

val LightPu = PuColors(
    bg = Color(0xFFFFFFFF),
    card = Color(0xFFF4F5F8),
    ink = Color(0xFF101A33),
    muted = Color(0xFF6B7280),
    chip = Color(0xFFE8EAF0),
    line = Color(0xFFD5D9E2),
    map = Color(0xFFE6EBF2)
)

val DarkPu = PuColors(
    bg = Color(0xFF101A33),
    card = Color(0xFF16223F),
    ink = Color(0xFFFFFFFF),
    muted = Color(0xFFB3BAC9),
    chip = Color(0xFF2A3A66),
    line = Color(0xFF3A4668),
    map = Color(0xFF1B2A4E)
)

val LocalPu = staticCompositionLocalOf { LightPu }

val PuBg: Color @Composable get() = LocalPu.current.bg
val PuCard: Color @Composable get() = LocalPu.current.card
val PuInk: Color @Composable get() = LocalPu.current.ink
val PuMuted: Color @Composable get() = LocalPu.current.muted
val PuChip: Color @Composable get() = LocalPu.current.chip
val PuLine: Color @Composable get() = LocalPu.current.line
val PuMap: Color @Composable get() = LocalPu.current.map

val PuAmber = Color(0xFFFF8A1E)
val PuAmberInk = Color(0xFF3A1E00)
val PuGood = Color(0xFF1FA463)
val PuDanger = Color(0xFFE5484D)

fun naira(amount: Int): String = "₦" + "%,d".format(amount)

fun perKm(r: RideRequest): Int = (r.fare / r.distanceKm).toInt()

fun roundTo100(x: Int): Int = ((x + 50) / 100) * 100

// PLACEHOLDERS until the real fee and tax rules are confirmed.
const val FLAT_FEE = 1000

/** 7.5% VAT on a fare (placeholder rule, to be confirmed with an accountant). */
fun vatOn(fare: Int): Int = (fare * 75 + 500) / 1000

/** "Fair fare" tag shows only on rides paying a high rate per km. */
fun isFairRate(r: RideRequest): Boolean = perKm(r) >= 500

/** The two counter prices a driver can ask for: 10% and 20% above the offer. */
fun counterOffers(fare: Int): List<Int> =
    listOf(110, 120).map { roundTo100(fare * it / 100) }.distinct().filter { it != fare }

@Composable
fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = PuInk,
    unfocusedTextColor = PuInk,
    focusedContainerColor = PuCard,
    unfocusedContainerColor = PuCard,
    focusedBorderColor = PuAmber,
    unfocusedBorderColor = PuLine,
    cursorColor = PuAmber
)

@Composable
fun PrimaryButton(text: String, onClick: () -> Unit, enabled: Boolean = true) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = PuAmber,
            contentColor = PuAmberInk,
            disabledContainerColor = Color(0x33FF8A1E),
            disabledContentColor = PuMuted
        )
    ) {
        Text(text, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun OutlineButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier.fillMaxWidth(),
    color: Color = PuAmber
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(52.dp),
        border = BorderStroke(1.dp, color)
    ) {
        Text(text, color = color, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun Pill(text: String, bg: Color, fg: Color, fontSize: TextUnit = 12.sp) {
    Box(
        modifier = Modifier
            .background(bg, RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(text, color = fg, fontSize = fontSize, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun InfoCard(content: @Composable ColumnScope.() -> Unit) {
    // Flat section with a thin line underneath, instead of a floating rounded card.
    Column(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(vertical = 10.dp), content = content)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(PuLine)
        )
    }
}

@Composable
fun SkeletonDialog(message: String, onOk: () -> Unit) {
    AlertDialog(
        onDismissRequest = onOk,
        confirmButton = { TextButton(onClick = onOk) { Text("OK", color = PuAmber) } },
        title = { Text("Skeleton build") },
        text = { Text(message) },
        containerColor = PuCard,
        titleContentColor = PuInk,
        textContentColor = PuMuted
    )
}

@Composable
fun RiderAvatar(name: String, size: Dp = 28.dp) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(PuChip),
        contentAlignment = Alignment.Center
    ) {
        Text(
            name.take(1).uppercase(),
            color = PuInk,
            fontSize = (size.value * 0.45f).sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun StopMarker(letter: String, color: Color) {
    Box(
        modifier = Modifier
            .size(22.dp)
            .clip(CircleShape)
            .background(color),
        contentAlignment = Alignment.Center
    ) {
        Text(letter, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

/** Stand-in for the real Google Map that comes later. */
@Composable
fun MapPlaceholder(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit = {}) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(PuMap)
    ) {
        Text(
            "Map view",
            color = PuMuted,
            fontSize = 14.sp,
            modifier = Modifier.align(Alignment.Center)
        )
        content()
    }
}

/** Copies text to the phone clipboard so it can be pasted straight into Google Maps. */
fun copyToClipboard(context: Context, text: String) {
    val manager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    manager.setPrimaryClip(ClipData.newPlainText("address", text))
    // Android 13+ shows its own "copied" message.
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
        Toast.makeText(context, "Address copied", Toast.LENGTH_SHORT).show()
    }
}

/** A pickup or drop-off line. Tapping it copies the address. */
@Composable
fun AddressRow(
    letter: String,
    markerColor: Color,
    address: String,
    textColor: Color = PuInk,
    fontSize: TextUnit = 15.sp,
    bold: Boolean = false
) {
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { copyToClipboard(context, address) },
        verticalAlignment = Alignment.CenterVertically
    ) {
        StopMarker(letter, markerColor)
        Spacer(Modifier.width(10.dp))
        Text(
            address,
            color = textColor,
            fontSize = fontSize,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.weight(1f)
        )
    }
}

/** The installed build, e.g. "0.1.26", so you can tell which APK is running. */
@Composable
fun appVersion(): String {
    val context = LocalContext.current
    return remember {
        try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "?"
        } catch (e: Exception) {
            "?"
        }
    }
}

/** Day / Week / Month style switch. */
@Composable
fun SegmentedToggle(options: List<String>, selected: Int, onSelect: (Int) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(50))
            .background(PuCard)
            .padding(4.dp)
    ) {
        options.forEachIndexed { index, label ->
            val on = index == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(50))
                    .background(if (on) PuAmber else Color.Transparent)
                    .clickable { onSelect(index) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    label,
                    color = if (on) PuAmberInk else PuMuted,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/** Small number-plus-caption tile used on Income and Performance. */
@Composable
fun StatTile(value: String, label: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .border(1.dp, PuLine, RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(value, color = PuInk, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text(label, color = PuMuted, fontSize = 12.sp)
    }
}

/** One label-and-amount line, used on the receipt, Income and Wallet. */
@Composable
fun MoneyRow(label: String, value: String, strong: Boolean = false, valueColor: Color = PuInk) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
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
            color = valueColor,
            fontSize = if (strong) 22.sp else 14.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

/** Round hamburger button that opens the side menu. */
@Composable
fun MenuButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(PuCard)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text("☰", color = PuInk, fontSize = 20.sp)
    }
}
