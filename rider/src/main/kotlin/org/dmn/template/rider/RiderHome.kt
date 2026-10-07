package org.dmn.template.rider

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.dmn.template.MapPlaceholder
import org.dmn.template.MenuButton
import org.dmn.template.PrimaryButton
import org.dmn.template.Pill
import org.dmn.template.PuBg
import org.dmn.template.PuChip
import org.dmn.template.PuGood
import org.dmn.template.PuInk
import org.dmn.template.PuMuted
import org.dmn.template.appVersion
import org.dmn.template.fieldColors

/** Map with a pin, the pickup and destination lines, and landmark search. */
@Composable
fun RiderHomeScreen(draft: RideDraft, onMenu: () -> Unit, onContinue: () -> Unit) {
    // 0 = overview, 1 = choosing the pickup, 2 = choosing the destination.
    var editing by remember { mutableIntStateOf(0) }
    var query by remember { mutableStateOf("") }

    Surface(modifier = Modifier.fillMaxSize(), color = PuBg) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .imePadding()
                .padding(horizontal = 20.dp)
        ) {
            if (editing == 0) {
                MapPlaceholder(Modifier.weight(1f).padding(top = 8.dp)) {
                    Text(
                        "📍",
                        fontSize = 36.sp,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(bottom = 64.dp)
                    )
                    Box(Modifier.align(Alignment.TopStart).padding(12.dp)) {
                        MenuButton(onMenu)
                    }
                    Box(Modifier.align(Alignment.BottomCenter).padding(bottom = 12.dp)) {
                        Pill("Move the map to set the pin", PuChip, PuInk, 12.sp)
                    }
                }
                Spacer(Modifier.height(4.dp))
                PlaceLine("A", RouteBlue, "Pickup", draft.pickup.name) {
                    query = ""
                    editing = 1
                }
                HairLine()
                PlaceLine("B", PuGood, "Destination", draft.dropoff?.name ?: "Where to?") {
                    query = ""
                    editing = 2
                }
                HairLine()
                Spacer(Modifier.height(12.dp))
                PrimaryButton("Continue", onContinue, enabled = draft.dropoff != null)
                Spacer(Modifier.height(8.dp))
                Text("Build ${appVersion()}", color = PuMuted, fontSize = 11.sp)
                Spacer(Modifier.height(8.dp))
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        if (editing == 1) "Pickup" else "Where to?",
                        color = PuInk,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = { editing = 0 }) {
                        Text("Close", color = PuMuted)
                    }
                }
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("Search a landmark or area", color = PuMuted) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = fieldColors()
                )
                Spacer(Modifier.height(8.dp))
                val matches = RiderDemo.places.filter {
                    query.isBlank() ||
                        it.name.contains(query, ignoreCase = true) ||
                        it.area.contains(query, ignoreCase = true)
                }
                if (matches.isEmpty()) {
                    Text(
                        "No match. Full search comes with the real map.",
                        color = PuMuted,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                }
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(matches) { place ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (editing == 1) draft.pickup = place else draft.dropoff = place
                                    editing = 0
                                }
                        ) {
                            Column(modifier = Modifier.padding(vertical = 10.dp)) {
                                Text(place.name, color = PuInk, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                Text(place.area, color = PuMuted, fontSize = 13.sp)
                            }
                            HairLine()
                        }
                    }
                }
            }
        }
    }
}
