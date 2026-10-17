package com.rajatxo.lutea.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rajatxo.lutea.ui.components.CalSansFamily

/**
 * QuickPicksScreen — the first page of Lutea after onboarding.
 *
 * Layout adapted from inspiration image (movie detail style → music Quick Picks):
 *   - Dark charcoal background (#1C1C1E)
 *   - "Quick Picks" title at top-left, large, bold
 *   - Subtitle/metadata below
 *   - Divider line
 *   - "Recently Played" section header
 *   - Horizontal scroll row of circular thumbnails (placeholder)
 *   - "Made for You" section header
 *   - Another horizontal scroll row
 */
@Composable
fun QuickPicksScreen(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF1C1C1E))
            .padding(horizontal = 20.dp)
            .padding(top = 56.dp, bottom = 24.dp),
    ) {
        // ── Title at top-left ──
        Text(
            text = "Quick Picks",
            color = Color.White,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = CalSansFamily,
        )

        Spacer(Modifier.height(4.dp))

        // ── Subtitle ──
        Text(
            text = "Your music, ready to play",
            color = Color(0xFF8E8E93),
            fontSize = 13.sp,
            fontWeight = FontWeight.Normal,
        )

        Spacer(Modifier.height(20.dp))

        // ── Divider ──
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.5.dp)
                .background(Color.White.copy(alpha = 0.1f)),
        )

        Spacer(Modifier.height(20.dp))

        // ── Section: Recently Played ──
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Recently Played",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = CalSansFamily,
            )
            Text(
                text = "See all",
                color = Color(0xFF8E8E93),
                fontSize = 13.sp,
            )
        }

        Spacer(Modifier.height(16.dp))

        // ── Horizontal row of circular thumbnails ──
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            repeat(4) { index ->
                CircularThumbPlaceholder()
            }
        }

        Spacer(Modifier.height(28.dp))

        // ── Section: Made for You ──
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Made for You",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = CalSansFamily,
            )
            Text(
                text = "See all",
                color = Color(0xFF8E8E93),
                fontSize = 13.sp,
            )
        }

        Spacer(Modifier.height(16.dp))

        // ── Horizontal row of circular thumbnails ──
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            repeat(4) { index ->
                CircularThumbPlaceholder()
            }
        }

        Spacer(Modifier.height(28.dp))

        // ── Section: Recently Added ──
        Text(
            text = "Recently Added",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = CalSansFamily,
        )

        Spacer(Modifier.height(16.dp))

        // ── List of song rows ──
        repeat(3) { index ->
            SongRowPlaceholder()
            Spacer(Modifier.height(12.dp))
        }
    }
}

/** Circular thumbnail with name placeholder — like the actor cards in the inspiration image. */
@Composable
private fun CircularThumbPlaceholder() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF2A2A2A),
                            Color(0xFF1A1A1A),
                        ),
                    ),
                ),
        )
        Spacer(Modifier.height(8.dp))
        // Name placeholder bar
        Box(
            modifier = Modifier
                .width(56.dp)
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color.White.copy(alpha = 0.12f)),
        )
    }
}

/** Song row — circular thumbnail + title/artist bars + duration. */
@Composable
private fun SongRowPlaceholder() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF2A2A2A),
                            Color(0xFF1A1A1A),
                        ),
                    ),
                ),
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .height(12.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.White.copy(alpha = 0.15f)),
            )
            Spacer(Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.35f)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.White.copy(alpha = 0.08f)),
            )
        }
        Box(
            modifier = Modifier
                .width(36.dp)
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color.White.copy(alpha = 0.08f)),
        )
    }
}
