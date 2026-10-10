package org.dmn.template.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.dmn.template.PuBg
import org.dmn.template.PuCard
import org.dmn.template.PuChip
import org.dmn.template.PuDanger
import org.dmn.template.PuInk
import org.dmn.template.RiderAvatar
import org.dmn.template.SkeletonDialog

@Composable
private fun TileView(tile: Tile, badge: Int, big: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box {
            Box(
                modifier = Modifier
                    .size(if (big) 60.dp else 48.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(PuChip),
                contentAlignment = Alignment.Center
            ) {
                Text(tile.glyph, fontSize = if (big) 28.sp else 22.sp)
            }
            if (badge > 0) {
                Text(
                    "$badge",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .clip(CircleShape)
                        .background(PuDanger)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            tile.label,
            color = PuInk,
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

/** A rounded panel of tiles, four across. */
@Composable
private fun TileGrid(
    tiles: List<Tile>,
    big: Boolean,
    badgeFor: (Tile) -> Int,
    onTile: (Tile) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(PuCard)
            .padding(horizontal = 8.dp, vertical = 8.dp)
    ) {
        tiles.chunked(4).forEach { row ->
            Row(modifier = Modifier.fillMaxWidth()) {
                row.forEach { tile ->
                    TileView(tile, badgeFor(tile), big, Modifier.weight(1f)) { onTile(tile) }
                }
                repeat(4 - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

/**
 * Every area is an icon, so any part is one tap away. Areas are numbered in build order, and only
 * the finished ones open for now.
 */
@Composable
fun AdminHomeScreen(onOpenStep: (Int) -> Unit) {
    var notBuilt by remember { mutableStateOf<Tile?>(null) }

    Surface(modifier = Modifier.fillMaxSize(), color = PuBg) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RiderAvatar("Admin", 44.dp)
                Spacer(Modifier.width(12.dp))
                Text("Hi, Admin", color = PuInk, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }

            val badgeFor: (Tile) -> Int = { tile ->
                when (tile.step) {
                    1 -> AdminRepository.pending.size
                    4 -> AdminRepository.openDisputes()
                    else -> 0
                }
            }
            val onTile: (Tile) -> Unit = { tile ->
                if (tile.step in builtSteps) onOpenStep(tile.step) else notBuilt = tile
            }
            TileGrid(adminTiles.take(4), big = true, badgeFor = badgeFor, onTile = onTile)
            Spacer(Modifier.height(12.dp))
            TileGrid(adminTiles.drop(4), big = false, badgeFor = badgeFor, onTile = onTile)
            Spacer(Modifier.height(16.dp))
        }
    }

    notBuilt?.let { tile ->
        SkeletonDialog("${tile.fullName} is step ${tile.step}. It is not built yet.") {
            notBuilt = null
        }
    }
}
