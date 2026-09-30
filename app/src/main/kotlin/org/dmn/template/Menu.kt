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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val menuItems = listOf(
    "Profile & documents",
    "Trip history",
    "Referral",
    "Safety",
    "Help & support"
)

@Composable
private fun MenuRow(label: String, color: Color = PuInk, onClick: () -> Unit) {
    Text(
        label,
        color = color,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp)
    )
}

@Composable
fun DriverDrawer(
    themeMode: Int,
    onThemeMode: (Int) -> Unit,
    onItem: (String) -> Unit,
    onLogout: () -> Unit
) {
    val rating = DriverRepository.performance(1).rating

    ModalDrawerSheet(drawerContainerColor = PuBg, drawerContentColor = PuInk) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                RiderAvatar("Driver", 56.dp)
                Spacer(Modifier.width(14.dp))
                Column {
                    Text("Your Name", color = PuInk, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text("★ $rating", color = PuMuted, fontSize = 13.sp)
                }
            }
            Spacer(Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(PuLine)
            )
            Spacer(Modifier.height(4.dp))

            menuItems.forEach { label ->
                MenuRow(label) { onItem(label) }
            }

            Spacer(Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(PuLine)
            )
            Spacer(Modifier.height(16.dp))
            Text("Appearance", color = PuMuted, fontSize = 13.sp)
            Spacer(Modifier.height(8.dp))
            SegmentedToggle(listOf("Auto", "Light", "Dark"), themeMode, onThemeMode)
            Spacer(Modifier.height(4.dp))
            Text("Auto follows your phone's light or dark setting.", color = PuMuted, fontSize = 12.sp)

            Spacer(Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(PuLine)
            )
            MenuRow("Log out", PuDanger, onLogout)
        }
    }
}
