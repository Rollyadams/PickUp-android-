package org.dmn.template

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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

val PuNavy = Color(0xFF101A33)
val PuNavy2 = Color(0xFF16223F)
val PuAmber = Color(0xFFFF8A1E)
val PuAmberInk = Color(0xFF3A1E00)
val PuMuted = Color(0xB3FFFFFF)
val PuDanger = Color(0xFFE5484D)

fun naira(amount: Int): String = "₦" + "%,d".format(amount)

fun perKm(r: RideRequest): Int = (r.fare / r.distanceKm).toInt()

fun roundTo100(x: Int): Int = ((x + 50) / 100) * 100

/** "Fair fare" tag shows only on rides paying a high rate per km. */
fun isFairRate(r: RideRequest): Boolean = perKm(r) >= 500

/** The two counter prices a driver can ask for: 10% and 20% above the offer. */
fun counterOffers(fare: Int): List<Int> =
    listOf(110, 120).map { roundTo100(fare * it / 100) }.distinct().filter { it != fare }

@Composable
fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    focusedContainerColor = PuNavy2,
    unfocusedContainerColor = PuNavy2,
    focusedBorderColor = PuAmber,
    unfocusedBorderColor = Color(0x66FFFFFF),
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
            disabledContentColor = Color(0x66FFFFFF)
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
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PuNavy2)
    ) {
        Column(modifier = Modifier.padding(16.dp), content = content)
    }
}

@Composable
fun SkeletonDialog(message: String, onOk: () -> Unit) {
    AlertDialog(
        onDismissRequest = onOk,
        confirmButton = { TextButton(onClick = onOk) { Text("OK", color = PuAmber) } },
        title = { Text("Skeleton build") },
        text = { Text(message) },
        containerColor = PuNavy2,
        titleContentColor = Color.White,
        textContentColor = PuMuted
    )
}

@Composable
fun RiderAvatar(name: String, size: Dp = 28.dp) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(Color(0xFF2A3A66)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            name.take(1).uppercase(),
            color = Color.White,
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
            .background(Color(0xFF1B2A4E))
    ) {
        Text(
            "Map view",
            color = Color(0x66FFFFFF),
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

/** A pickup or drop-off line. Tap it to copy the address. */
@Composable
fun AddressRow(
    letter: String,
    markerColor: Color,
    address: String,
    textColor: Color = Color.White,
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
        Text("Copy", color = PuMuted, fontSize = 11.sp)
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
