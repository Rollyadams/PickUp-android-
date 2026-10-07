package org.dmn.template.rider

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.dmn.template.MapPlaceholder
import org.dmn.template.MenuButton
import org.dmn.template.PuAmber
import org.dmn.template.PuBg
import org.dmn.template.PuCard
import org.dmn.template.PuChip
import org.dmn.template.PuInk
import org.dmn.template.PuMuted
import org.dmn.template.appVersion

/**
 * The first screen. The pickup is found from the phone's location and the pin can be corrected by
 * hand. The real map comes later, so for now the map area is a placeholder.
 */
@Composable
fun RiderHomeScreen(
    draft: RideDraft,
    onMenu: () -> Unit,
    onEditPickup: () -> Unit,
    onWhereTo: () -> Unit
) {
    val context = LocalContext.current
    var detectTick by remember { mutableIntStateOf(0) }
    var status by remember { mutableStateOf("") }
    val permissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )
    val askPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        if (result.values.any { it }) detectTick++ else status = "denied"
    }

    // Ask once, the first time the rider has no pickup yet.
    LaunchedEffect(Unit) {
        if (draft.pickup == null && !hasLocationPermission(context)) {
            askPermission.launch(permissions)
        }
    }

    // Find the rider. Skips when a pickup already exists, unless the rider tapped "find me".
    LaunchedEffect(detectTick) {
        if (!hasLocationPermission(context)) return@LaunchedEffect
        if (detectTick == 0 && draft.pickup != null) return@LaunchedEffect
        status = "locating"
        val point = currentPoint(context)
        if (point == null) {
            status = "failed"
            return@LaunchedEffect
        }
        draft.pickup = addressFor(context, point) ?: Place("Current location", "", point.lat, point.lng)
        draft.pickupIsManual = false
        status = ""
    }

    val pickup = draft.pickup
    val bubbleText = when {
        pickup != null -> pickup.name
        status == "denied" || status == "failed" -> "Choose your pickup"
        else -> "Finding you…"
    }

    Surface(modifier = Modifier.fillMaxSize(), color = PuBg) {
        Column(modifier = Modifier.fillMaxSize().systemBarsPadding()) {
            MapPlaceholder(
                Modifier
                    .weight(1f)
                    .padding(start = 12.dp, end = 12.dp, top = 8.dp)
            ) {
                // Pin and the bubble above it, kept above the centre so they never sit on the map label.
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(bottom = 96.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier
                            .widthIn(max = 260.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(PuCard)
                            .clickable(onClick = onEditPickup)
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f, fill = false)) {
                            Text("Pickup", color = PuMuted, fontSize = 12.sp)
                            Text(
                                bubbleText,
                                color = PuInk,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        Text("›", color = PuMuted, fontSize = 20.sp)
                    }
                    Text("📍", fontSize = 34.sp)
                }
                Box(modifier = Modifier.align(Alignment.TopStart).padding(12.dp)) {
                    MenuButton(onMenu)
                }
                // Find me again, for when the detected spot is wrong.
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(12.dp)
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(PuCard)
                        .clickable {
                            if (hasLocationPermission(context)) {
                                detectTick++
                            } else {
                                askPermission.launch(permissions)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text("➤", color = PuInk, fontSize = 20.sp)
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    RiderDemo.rideTypes.forEach { type ->
                        val selected = draft.rideType == type.id
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (selected) PuChip else PuBg)
                                .border(
                                    1.dp,
                                    if (selected) PuAmber else PuChip,
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { draft.rideType = type.id }
                                .padding(horizontal = 6.dp, vertical = 10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(type.glyph, fontSize = 22.sp)
                            Text(
                                type.name,
                                color = PuInk,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(PuChip)
                        .clickable(onClick = onWhereTo)
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🔍", fontSize = 18.sp)
                    Spacer(Modifier.width(12.dp))
                    Text("Where to?", color = PuInk, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(8.dp))
                Text("Build ${appVersion()}", color = PuMuted, fontSize = 11.sp)
            }
        }
    }
}
