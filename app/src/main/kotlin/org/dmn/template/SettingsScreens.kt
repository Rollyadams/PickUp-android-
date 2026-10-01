package org.dmn.template

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
private fun SubScreen(title: String, onBack: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    Surface(modifier = Modifier.fillMaxSize(), color = PuBg) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = onBack) {
                    Text("←", color = PuInk, fontSize = 24.sp)
                }
                Spacer(Modifier.width(4.dp))
                Text(title, color = PuInk, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(8.dp))
            content()
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SettingRow(title: String, subtitle: String? = null, onClick: (() -> Unit)? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = PuInk, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            if (subtitle != null) Text(subtitle, color = PuMuted, fontSize = 13.sp)
        }
        if (onClick != null) Text("›", color = PuMuted, fontSize = 22.sp)
    }
}

@Composable
private fun SwitchRow(title: String, subtitle: String?, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onChange(!checked) }
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = PuInk, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            if (subtitle != null) Text(subtitle, color = PuMuted, fontSize = 13.sp)
        }
        Spacer(Modifier.width(8.dp))
        Switch(
            checked = checked,
            onCheckedChange = onChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = PuAmberInk,
                checkedTrackColor = PuAmber,
                uncheckedThumbColor = PuMuted,
                uncheckedTrackColor = PuCard,
                uncheckedBorderColor = PuLine
            )
        )
    }
}

// ---------------------------------------------------------------- Settings

@Composable
fun SettingsScreen(
    themeMode: Int,
    onThemeMode: (Int) -> Unit,
    navApp: Int,
    onNavApp: (Int) -> Unit,
    soundsOn: Boolean,
    onSoundsOn: (Boolean) -> Unit,
    keepScreenOn: Boolean,
    onKeepScreenOn: (Boolean) -> Unit,
    onBack: () -> Unit,
    onLogout: () -> Unit
) {
    var dialog by remember { mutableStateOf<String?>(null) }
    var showDelete by remember { mutableStateOf(false) }

    SubScreen("Settings", onBack) {
        InfoCard {
            Text("Appearance", color = PuInk, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            SegmentedToggle(listOf("Auto", "Light", "Dark"), themeMode, onThemeMode)
            Spacer(Modifier.height(4.dp))
            Text("Auto follows your phone's light or dark setting.", color = PuMuted, fontSize = 12.sp)
        }
        Spacer(Modifier.height(12.dp))

        InfoCard {
            Text("Navigation app", color = PuInk, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            SegmentedToggle(listOf("Google Maps", "Waze"), navApp, onNavApp)
            Spacer(Modifier.height(4.dp))
            Text("Used by the Navigate button. Waze must be installed.", color = PuMuted, fontSize = 12.sp)
        }
        Spacer(Modifier.height(12.dp))

        InfoCard {
            SwitchRow("Ride request sounds", "Play a sound for new requests (sound comes later)", soundsOn, onSoundsOn)
            SwitchRow("Do not lock the screen", "Keeps the screen on while Pick Up is open", keepScreenOn, onKeepScreenOn)
            SettingRow("Distance units", "Kilometres")
            SettingRow("Language", "English")
            SettingRow("In-app calls") { dialog = "In-app calls come in a later batch." }
        }
        Spacer(Modifier.height(12.dp))

        InfoCard {
            SettingRow("Legal documents") { dialog = "Terms and Privacy Policy will open here." }
            SettingRow("App version", appVersion())
        }
        Spacer(Modifier.height(16.dp))

        OutlineButton("Log out", onLogout, color = PuDanger)
        Spacer(Modifier.height(10.dp))
        OutlineButton("Delete account", { showDelete = true }, color = PuDanger)
    }

    dialog?.let { message -> SkeletonDialog(message = message, onOk = { dialog = null }) }
    if (showDelete) {
        AlertDialog(
            onDismissRequest = { showDelete = false },
            confirmButton = {
                TextButton(onClick = { showDelete = false; onLogout() }) {
                    Text("Delete", color = PuDanger)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDelete = false }) {
                    Text("Keep my account", color = PuAmber)
                }
            },
            title = { Text("Delete your account?") },
            text = { Text("This removes your profile and documents and cannot be undone. Skeleton build: nothing is deleted yet.") },
            containerColor = PuCard,
            titleContentColor = PuInk,
            textContentColor = PuMuted
        )
    }
}

// ----------------------------------------------------------- Notifications

@Composable
fun NotificationsScreen(onBack: () -> Unit) {
    val items = remember { DriverRepository.notifications() }

    SubScreen("Notifications", onBack) {
        items.forEach { item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .padding(top = 6.dp)
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(if (item.unread) PuAmber else Color.Transparent)
                )
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(item.title, color = PuInk, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text(item.body, color = PuMuted, fontSize = 14.sp)
                    Spacer(Modifier.height(2.dp))
                    Text(item.time, color = PuMuted, fontSize = 12.sp)
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(PuChip)
            )
        }
    }
}

// ------------------------------------------------------------------ Safety

@Composable
fun SafetyScreen(onBack: () -> Unit, onSupport: () -> Unit) {
    val context = LocalContext.current
    val locationOk = rememberAlwaysLocationGranted()
    val requestLocation = rememberLocationRequester()
    var shareTrip by remember { mutableStateOf(false) }
    var dialog by remember { mutableStateOf<String?>(null) }

    SubScreen("Safety", onBack) {
        InfoCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Location",
                    color = PuInk, fontSize = 16.sp, fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                if (locationOk) Pill("On all the time ✓", PuGood, Color.White) else Pill("Needs fixing", PuAmber, PuAmberInk)
            }
            Spacer(Modifier.height(6.dp))
            Text(
                "Pick Up uses your location while you are online so riders can find you and help can reach you fast. Trip locations are recorded for safety.",
                color = PuMuted, fontSize = 13.sp
            )
            if (!locationOk) {
                Spacer(Modifier.height(12.dp))
                OutlineButton("Allow all the time", { requestLocation() })
                TextButton(onClick = { openAppSettings(context) }) {
                    Text("Open phone settings", color = PuMuted)
                }
            }
        }
        Spacer(Modifier.height(12.dp))

        InfoCard {
            SettingRow("SOS contacts", "Add up to 2 people we alert if you press SOS") {
                dialog = "Adding SOS contacts comes in a later batch."
            }
            SwitchRow("Share my trip", "Send live trip details to a trusted contact", shareTrip) { shareTrip = it }
        }
        Spacer(Modifier.height(12.dp))

        InfoCard {
            Text("In an emergency", color = PuInk, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text(
                "During a trip, use SOS on the trip screen. For immediate danger, call the emergency services.",
                color = PuMuted, fontSize = 13.sp
            )
            Spacer(Modifier.height(12.dp))
            OutlineButton("Call emergency services (112)", {
                try {
                    context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:112")))
                } catch (e: ActivityNotFoundException) {
                    dialog = "No phone app found on this device."
                }
            }, color = PuDanger)
            Spacer(Modifier.height(10.dp))
            OutlineButton("Report a problem", onSupport)
        }
    }

    dialog?.let { message -> SkeletonDialog(message = message, onOk = { dialog = null }) }
}

// -------------------------------------------------------------------- Help

private data class Faq(val question: String, val answer: String)

private val faqs = listOf(
    Faq(
        "Why can't I go online?",
        "You need approved documents, valid insurance and roadworthiness papers, and your phone's location set to Allow all the time."
    ),
    Faq(
        "How does the flat fee work?",
        "You pay a flat fee for each day you drive. Pick Up takes no commission from your fares. VAT is collected separately, and both show in your Wallet."
    ),
    Faq(
        "How are fares set?",
        "Riders offer a price. You can accept it or ask for a higher one. The per-km rate is on every request so you can decide quickly."
    ),
    Faq(
        "How do I get Comfort rides?",
        "After each trip, riders confirm whether the AC worked and the car was clean. A steady run of good reports makes you eligible for Comfort rides."
    ),
    Faq(
        "What if I feel unsafe?",
        "Press SOS on the trip screen. It alerts Pick Up support. You can also call the emergency services from the Safety screen."
    )
)

@Composable
fun HelpScreen(onBack: () -> Unit) {
    var open by rememberSaveable { mutableStateOf(-1) }

    SubScreen("Help", onBack) {
        InfoCard {
            faqs.forEachIndexed { index, faq ->
                val expanded = open == index
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { open = if (expanded) -1 else index }
                        .padding(vertical = 12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            faq.question,
                            color = PuInk, fontSize = 16.sp, fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        Text(if (expanded) "−" else "+", color = PuMuted, fontSize = 22.sp)
                    }
                    if (expanded) {
                        Spacer(Modifier.height(6.dp))
                        Text(faq.answer, color = PuMuted, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------------------- Support

@Composable
fun SupportScreen(onBack: () -> Unit) {
    var dialog by remember { mutableStateOf<String?>(null) }

    SubScreen("Support", onBack) {
        InfoCard {
            Text("How can we help?", color = PuInk, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text(
                "We aim to reply within a day. In an emergency, use SOS or call the emergency services instead.",
                color = PuMuted, fontSize = 13.sp
            )
            Spacer(Modifier.height(14.dp))
            PrimaryButton("Chat with support", { dialog = "Support chat comes in a later batch." })
            Spacer(Modifier.height(10.dp))
            OutlineButton("Call support", { dialog = "Calling support comes in a later batch." })
            Spacer(Modifier.height(10.dp))
            OutlineButton("Report a problem with a trip", { dialog = "Trip reports come in a later batch." })
        }
    }

    dialog?.let { message -> SkeletonDialog(message = message, onOk = { dialog = null }) }
}

// ------------------------------------------------------------------ Invite

@Composable
fun InviteScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val code = "PICKUP-4821"

    SubScreen("Invite a friend", onBack) {
        InfoCard {
            Text("Your invite code", color = PuMuted, fontSize = 13.sp)
            Text(code, color = PuInk, fontSize = 30.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            Text(
                "Friends who drive with Pick Up can enter your code when they sign up. Rewards for inviting are coming soon.",
                color = PuMuted, fontSize = 13.sp
            )
            Spacer(Modifier.height(14.dp))
            PrimaryButton("Share my code", {
                val send = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, "Drive with Pick Up. Use my invite code $code when you sign up.")
                }
                context.startActivity(Intent.createChooser(send, null))
            })
        }
    }
}

// ----------------------------------------------------------------- Ratings

private data class Review(val stars: Int, val comment: String?, val tags: List<String>)
private data class ReviewDay(val date: String, val reviews: List<Review>)

private val reviewDays = listOf(
    ReviewDay(
        "26 Sep 2026",
        listOf(
            Review(5, "Excellent. I also enjoy his driving and he is calm too.", listOf("Was excellent", "AC working")),
            Review(5, null, listOf("Was excellent", "Clean car"))
        )
    ),
    ReviewDay(
        "25 Sep 2026",
        listOf(Review(5, "Amazing driver.", listOf("Was excellent", "Clean car", "AC working")))
    ),
    ReviewDay(
        "24 Sep 2026",
        listOf(
            Review(4, null, listOf("AC not working")),
            Review(5, "Smooth trip.", listOf("Clean car"))
        )
    )
)

private fun stars(count: Int): String = "★".repeat(count) + "☆".repeat(5 - count)

@Composable
fun RatingsScreen(onBack: () -> Unit) {
    val rating = DriverRepository.performance(1).rating
    var showTips by remember { mutableStateOf(false) }

    // PLACEHOLDER numbers until real rider reports exist.
    val acRate = 92
    val cleanRate = 96

    SubScreen("Rating and feedback", onBack) {
        InfoCard {
            Text("★ $rating", color = PuInk, fontSize = 40.sp, fontWeight = FontWeight.Bold)
            Text("Based on the last 25 reviews", color = PuMuted, fontSize = 14.sp)
        }
        Spacer(Modifier.height(12.dp))

        InfoCard {
            Text("Car checks from riders", color = PuInk, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            MoneyRow("AC working", "$acRate% of trips")
            MoneyRow("Car clean", "$cleanRate% of trips")
            Spacer(Modifier.height(6.dp))
            if (acRate >= 85) Pill("AC verified by riders ✓", PuGood, Color.White)
            Spacer(Modifier.height(8.dp))
            Text(
                "Riders confirm this after every trip. A steady run of good reports unlocks Comfort rides.",
                color = PuMuted, fontSize = 12.sp
            )
        }
        Spacer(Modifier.height(12.dp))

        InfoCard {
            SettingRow("Tips for success") { showTips = true }
        }
        Spacer(Modifier.height(16.dp))

        reviewDays.forEach { day ->
            Text(day.date, color = PuMuted, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            InfoCard {
                day.reviews.forEachIndexed { index, review ->
                    if (index > 0) Spacer(Modifier.height(14.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RiderAvatar("Passenger", 32.dp)
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text("Passenger", color = PuInk, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            Text(stars(review.stars), color = PuAmber, fontSize = 15.sp)
                        }
                    }
                    if (review.comment != null) {
                        Spacer(Modifier.height(6.dp))
                        Text(review.comment, color = PuInk, fontSize = 14.sp)
                    }
                    Spacer(Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        review.tags.forEach { tag ->
                            if (tag.contains("not")) {
                                Pill(tag, PuDanger, Color.White, 11.sp)
                            } else {
                                Pill(tag, PuChip, PuInk, 11.sp)
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
        }
    }

    if (showTips) {
        SkeletonDialog(
            message = "Keep the car clean and the AC working. Be polite and calm. Do not cancel after accepting. Arrive at the pickup point on time.",
            onOk = { showTips = false }
        )
    }
}
