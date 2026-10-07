package org.dmn.template.rider

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import org.dmn.template.PuBg
import org.dmn.template.PuChip
import org.dmn.template.PuInk
import org.dmn.template.PuMuted
import org.dmn.template.fieldColors

private fun matches(place: Place, text: String): Boolean =
    place.name.contains(text, ignoreCase = true) || place.area.contains(text, ignoreCase = true)

@Composable
private fun FieldBlock(
    label: String,
    value: String,
    placeholder: String,
    active: Boolean,
    focus: FocusRequester,
    onValue: (String) -> Unit,
    onActivate: () -> Unit
) {
    if (active) {
        OutlinedTextField(
            value = value,
            onValueChange = onValue,
            label = { Text(label, color = PuMuted) },
            placeholder = { Text(placeholder, color = PuMuted, maxLines = 1) },
            singleLine = true,
            trailingIcon = {
                if (value.isNotEmpty()) {
                    Text(
                        "✕",
                        color = PuMuted,
                        fontSize = 16.sp,
                        modifier = Modifier.clickable { onValue("") }.padding(12.dp)
                    )
                }
            },
            modifier = Modifier.fillMaxWidth().focusRequester(focus),
            colors = fieldColors()
        )
    } else {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(PuChip)
                .clickable(onClick = onActivate)
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Text(label, color = PuMuted, fontSize = 12.sp)
            Text(
                value.ifEmpty { placeholder },
                color = PuInk,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Type a pickup or a stop and results appear from the map's search as you type.
 * If the phone's search finds nothing (or there is no internet), a few demo places are offered.
 */
@Composable
fun RiderSearchScreen(draft: RideDraft, onClose: () -> Unit, onPicked: () -> Unit) {
    val context = LocalContext.current
    // 0 = editing the pickup, 1 = editing the destination or stop.
    var active by remember { mutableIntStateOf(draft.searchTarget) }
    var pickupText by remember { mutableStateOf("") }
    var stopText by remember { mutableStateOf("") }
    var results by remember { mutableStateOf<List<Place>>(emptyList()) }
    var needPickupFirst by remember { mutableStateOf(false) }
    val focus = remember { FocusRequester() }
    val query = if (active == 0) pickupText else stopText

    LaunchedEffect(active) {
        try {
            focus.requestFocus()
        } catch (e: Exception) {
            // The field was not on screen yet; the rider can tap it.
        }
    }

    LaunchedEffect(active, query) {
        val text = query.trim()
        if (text.length < 3) {
            results = RiderDemo.places.filter { text.isEmpty() || matches(it, text) }
            return@LaunchedEffect
        }
        delay(450)
        val near = draft.pickup?.let { GeoPoint(it.lat, it.lng) }
        val found = searchPlaces(context, text, near)
        results = found.ifEmpty { RiderDemo.places.filter { matches(it, text) } }
    }

    fun pick(place: Place) {
        if (active == 0) {
            draft.pickup = place
            draft.pickupIsManual = true
            needPickupFirst = false
            if (draft.stops.isNotEmpty()) onPicked() else active = 1
        } else if (draft.pickup == null) {
            needPickupFirst = true
            active = 0
        } else {
            val index = draft.searchStopIndex
            if (index < draft.stops.size) draft.stops[index] = place else draft.stops.add(place)
            onPicked()
        }
    }

    val from = draft.pickup

    Surface(modifier = Modifier.fillMaxSize(), color = PuBg) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .imePadding()
                .padding(horizontal = 20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Plan your trip",
                    color = PuInk,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(PuChip)
                        .clickable(onClick = onClose),
                    contentAlignment = Alignment.Center
                ) {
                    Text("✕", color = PuInk, fontSize = 18.sp)
                }
            }
            FieldBlock(
                label = "From",
                value = pickupText,
                placeholder = from?.name ?: "Your pickup",
                active = active == 0,
                focus = focus,
                onValue = { pickupText = it },
                onActivate = { active = 0 }
            )
            Spacer(Modifier.height(8.dp))
            FieldBlock(
                label = "To",
                value = stopText,
                placeholder = "Where to?",
                active = active == 1,
                focus = focus,
                onValue = { stopText = it },
                onActivate = { active = 1 }
            )
            Spacer(Modifier.height(8.dp))
            if (needPickupFirst) {
                Text(
                    "Choose your pickup first.",
                    color = PuMuted,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
            HairLine()
            if (results.isEmpty() && query.trim().length >= 3) {
                Text(
                    "No match yet. Try another name or area.",
                    color = PuMuted,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(results) { place ->
                    Column(modifier = Modifier.fillMaxWidth().clickable { pick(place) }) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("📍", fontSize = 18.sp)
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    place.name,
                                    color = PuInk,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (place.area.isNotEmpty()) {
                                    Text(place.area, color = PuMuted, fontSize = 13.sp, maxLines = 1)
                                }
                            }
                            if (active == 1 && from != null) {
                                Spacer(Modifier.width(10.dp))
                                Text(
                                    "%.1f km".format(roadKm(from, place)),
                                    color = PuMuted,
                                    fontSize = 14.sp
                                )
                            }
                        }
                        HairLine()
                    }
                }
            }
        }
    }
}
