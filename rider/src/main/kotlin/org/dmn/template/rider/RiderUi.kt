package org.dmn.template.rider

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.dmn.template.PuInk
import org.dmn.template.PuLine
import org.dmn.template.PuMuted
import org.dmn.template.StopMarker

/** Blue "A" pickup marker, matching the driver app. */
internal val RouteBlue = Color(0xFF3B6EF5)

/** Thin line between rows, used instead of gaps and boxes. */
@Composable
internal fun HairLine() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(PuLine)
    )
}

/** Back arrow and title. */
@Composable
internal fun TopBar(title: String, onBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextButton(onClick = onBack) {
            Text("←", color = PuInk, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }
        Text(title, color = PuInk, fontSize = 20.sp, fontWeight = FontWeight.Bold)
    }
}

/** Label on the left, current value and an arrow on the right. Tapping it opens a choice. */
@Composable
internal fun ValueRow(label: String, value: String, onClick: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, color = PuInk, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.weight(1f))
            Text(value, color = PuMuted, fontSize = 14.sp)
            Spacer(Modifier.width(8.dp))
            Text("›", color = PuMuted, fontSize = 20.sp)
        }
        HairLine()
    }
}

/** A pickup or drop-off line with its A or B marker. */
@Composable
internal fun PlaceLine(letter: String, color: Color, label: String, value: String, onClick: (() -> Unit)? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        StopMarker(letter, color)
        Spacer(Modifier.width(12.dp))
        Column {
            Text(label, color = PuMuted, fontSize = 12.sp)
            Text(value, color = PuInk, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}
