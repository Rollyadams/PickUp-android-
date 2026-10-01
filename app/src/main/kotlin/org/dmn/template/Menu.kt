package org.dmn.template

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private data class MenuEntry(val route: String, val glyph: String, val label: String)

private val menuEntries = listOf(
    MenuEntry("history", "🕘", "Trip history"),
    MenuEntry("notifications", "🔔", "Notifications"),
    MenuEntry("safety", "🛡️", "Safety"),
    MenuEntry("settings", "⚙️", "Settings"),
    MenuEntry("help", "❓", "Help"),
    MenuEntry("support", "💬", "Support"),
    MenuEntry("invite", "🎁", "Invite a friend")
)

@Composable
fun DriverDrawer(
    unread: Int,
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit
) {
    val rating = DriverRepository.performance(1).rating
    val trips = DriverRepository.income(2).orders

    ModalDrawerSheet(drawerContainerColor = PuBg, drawerContentColor = PuInk) {
        Column(modifier = Modifier.fillMaxHeight()) {
            // Tapping the header opens the driver's profile.
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigate("profile") }
                    .padding(horizontal = 20.dp, vertical = 18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RiderAvatar("Driver", 56.dp)
                Spacer(Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Your Name", color = PuInk, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text("★ $rating · $trips trips", color = PuMuted, fontSize = 13.sp)
                }
                Text("›", color = PuMuted, fontSize = 24.sp)
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(PuLine)
            )

            menuEntries.forEach { entry ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigate(entry.route) }
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(entry.glyph, fontSize = 20.sp)
                    Spacer(Modifier.width(16.dp))
                    Text(
                        entry.label,
                        color = PuInk,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    if (entry.route == "notifications" && unread > 0) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(PuDanger),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("$unread", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(Modifier.weight(1f))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(PuLine)
            )
            Text(
                "Log out",
                color = PuDanger,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onLogout)
                    .padding(horizontal = 20.dp, vertical = 18.dp)
            )
        }
    }
}
