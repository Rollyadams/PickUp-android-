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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.dmn.template.PuAmber
import org.dmn.template.PuAmberInk
import org.dmn.template.PuChip
import org.dmn.template.PuInk
import org.dmn.template.PuLine
import org.dmn.template.PuMuted

/** Blue "A" pickup marker, matching the driver screens. */
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
        modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextButton(onClick = onBack) {
            Text("←", color = PuInk, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }
        Text(
            title,
            color = PuInk,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
    }
}

/** Label on the left, current value and an arrow on the right. */
@Composable
internal fun ValueRow(label: String, value: String, onClick: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                label,
                color = PuInk,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            if (value.isNotEmpty()) {
                Text(value, color = PuMuted, fontSize = 14.sp)
                Spacer(Modifier.width(8.dp))
            }
            Text("›", color = PuMuted, fontSize = 20.sp)
        }
        HairLine()
    }
}

/** Amber action button that can share a row with other things. */
@Composable
internal fun AmberButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(52.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = PuAmber,
            contentColor = PuAmberInk,
            disabledContainerColor = Color(0x33FF8A1E),
            disabledContentColor = PuMuted
        )
    ) {
        Text(text, fontSize = 15.sp, fontWeight = FontWeight.Bold, maxLines = 1)
    }
}

/** Small square button with one symbol. */
@Composable
internal fun IconBox(glyph: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(52.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(PuChip)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(glyph, fontSize = 22.sp)
    }
}

/** m:ss for a number of seconds. */
internal fun mmss(totalSeconds: Int): String =
    "${totalSeconds / 60}:${"%02d".format(totalSeconds % 60)}"
