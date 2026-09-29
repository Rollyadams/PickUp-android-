package org.dmn.template

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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
fun Pill(text: String, bg: Color, fg: Color) {
    Box(
        modifier = Modifier
            .background(bg, RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(text, color = fg, fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
