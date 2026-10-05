package org.dmn.template

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val tabRoutes = listOf("home", "income", "wallet", "performance")

private data class TabItem(val route: String, val glyph: String, val label: String)

private val tabItems = listOf(
    TabItem("home", "🏠", "Home"),
    TabItem("income", "💵", "Income"),
    TabItem("wallet", "👛", "Wallet"),
    TabItem("performance", "📊", "Performance")
)

@Composable
fun DriverBottomBar(current: String?, onSelect: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().background(PuBg)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(PuLine)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
        ) {
            tabItems.forEach { item ->
                val selected = item.route == current
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onSelect(item.route) }
                        .padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(item.glyph, fontSize = 20.sp)
                    Text(
                        item.label,
                        color = if (selected) PuAmber else PuMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

private fun ratingTrend(stats: PerformanceStats): Pair<String, Boolean> {
    val diff = stats.rating - stats.previousRating
    val up = diff >= 0
    val arrow = if (up) "▲" else "▼"
    return Pair("$arrow ${"%.2f".format(kotlin.math.abs(diff))} vs the period before", up)
}

@Composable
fun PerformanceScreen(onMenu: () -> Unit, onReviews: () -> Unit) {
    var window by remember { mutableIntStateOf(0) }
    val stats = DriverRepository.performance(window)
    val (trendText, trendUp) = ratingTrend(stats)

    Surface(modifier = Modifier.fillMaxSize(), color = PuBg) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                MenuButton(onClick = onMenu)
                Spacer(Modifier.width(12.dp))
                Text("Performance", color = PuInk, fontSize = 26.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(14.dp))
            SegmentedToggle(listOf("7 days", "30 days", "90 days"), window) { window = it }
            Spacer(Modifier.height(16.dp))

            InfoCard {
                Text("Rating", color = PuMuted, fontSize = 14.sp)
                Text("★ ${stats.rating}", color = PuInk, fontSize = 40.sp, fontWeight = FontWeight.Bold)
                Text(
                    trendText,
                    color = if (trendUp) PuGood else PuDanger,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(12.dp))
                OutlineButton("See reviews", onReviews)
            }
            Spacer(Modifier.height(4.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatTile("${stats.acceptance}%", "Acceptance rate", Modifier.weight(1f))
                StatTile("${stats.completion}%", "Completion rate", Modifier.weight(1f))
            }
            Spacer(Modifier.height(10.dp))
            StatTile("${stats.hoursOnline} h", "Hours online", Modifier.fillMaxWidth())
            Spacer(Modifier.height(16.dp))
            Text(
                "Acceptance is the share of requests you accept. Completion is the share of accepted rides you finish.",
                color = PuMuted, fontSize = 12.sp
            )
        }
    }
}
