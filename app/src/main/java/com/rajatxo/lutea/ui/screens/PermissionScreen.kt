package com.rajatxo.lutea.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.colorControls
import com.kyant.backdrop.effects.vibrancy

/**
 * PermissionScreen — shows after the greeting.
 *
 * Layout (per Opal inspiration screenshot):
 *   1. Old greeting text (dissolved/dimmed at top — handled by NameCardScreen)
 *   2. "I can help with that, [name]." — primary heading, white, blur-reveal
 *   3. "But to find your music, I'm going to need your permission." — subheading
 *   4. Permission card 1: Media files — toggle for READ_MEDIA_AUDIO
 *   5. Permission card 2: Notifications — toggle for POST_NOTIFICATIONS
 *   6. Continue button at bottom
 *
 * Permission cards use kyant frosted glass (drawBackdrop) — same as capsule/button.
 * Tapping a toggle triggers the actual Android system permission dialog.
 */
@Composable
fun PermissionScreen(
    displayName: String,
    onPermissionsComplete: () -> Unit,
    backdrop: com.kyant.backdrop.backdrops.LayerBackdrop,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    // ── Permission states ──────────────────────────────────────────────
    var mediaGranted by remember { mutableStateOf(false) }
    var notificationsGranted by remember { mutableStateOf(false) }

    // ── Permission launchers ───────────────────────────────────────────
    // Media permission: READ_MEDIA_AUDIO on API 33+, READ_EXTERNAL_STORAGE on older.
    val mediaPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(Manifest.permission.READ_MEDIA_AUDIO)
    } else {
        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
    }
    val mediaLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        mediaGranted = result.values.all { it }
    }

    // Notifications permission: POST_NOTIFICATIONS on API 33+.
    // On older versions, notifications don't need runtime permission.
    val needsNotificationPermission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
    val notificationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        notificationsGranted = granted
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // ── Permission cards only (heading text is handled by NameCardScreen) ──

        Spacer(Modifier.height(8.dp))

        // ── Permission card 1: Media files ──────────────────────────────
        AnimatedVisibility(
            visible = true,
            enter = fadeIn(tween(600)) +
                slideInVertically(
                    initialOffsetY = { it / 4 },
                    animationSpec = tween(600),
                ),
        ) {
            PermissionCard(
                title = "Media files",
                description = "So Lutea can find and play your music.",
                isGranted = mediaGranted,
                onToggle = {
                    if (!mediaGranted) {
                        mediaLauncher.launch(mediaPermission)
                    }
                },
                backdrop = backdrop,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        // ── Permission card 2: Notifications ───────────────────────────
        AnimatedVisibility(
            visible = true,
            enter = fadeIn(tween(600)) +
                slideInVertically(
                    initialOffsetY = { it / 4 },
                    animationSpec = tween(600),
                ),
        ) {
            PermissionCard(
                title = "Notifications",
                description = "So Lutea can show playback controls in your notifications.",
                isGranted = notificationsGranted,
                onToggle = {
                    if (!notificationsGranted && needsNotificationPermission) {
                        notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else if (!needsNotificationPermission) {
                        // On older Android, notifications don't need permission.
                        notificationsGranted = true
                    }
                },
                backdrop = backdrop,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        Spacer(Modifier.height(8.dp))

        // ── Continue button ─────────────────────────────────────────────
        AnimatedVisibility(
            visible = true,
            enter = fadeIn(tween(400)),
        ) {
            val canContinue = mediaGranted && notificationsGranted  // BOTH required
            ContinueButton(
                enabled = canContinue,
                onClick = onPermissionsComplete,
                backdrop = backdrop,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

/**
 * Permission toggle card — kyant frosted glass with title, description, and switch.
 */
@Composable
private fun PermissionCard(
    title: String,
    description: String,
    isGranted: Boolean,
    onToggle: () -> Unit,
    backdrop: com.kyant.backdrop.backdrops.LayerBackdrop,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(20.dp)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .drawBackdrop(
                backdrop = backdrop,
                shape = { shape },
                effects = {
                    vibrancy()
                    colorControls(
                        brightness = 0.05f,
                        contrast = 1f,
                        saturation = 1.4f,
                    )
                    blur(16f.dp.toPx())
                },
                onDrawSurface = {
                    drawRect(Color.Black.copy(alpha = 0.3f))
                },
            )
            .clickable { onToggle() }
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = description,
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
            )
        }
        Spacer(Modifier.width(16.dp))
        Switch(
            checked = isGranted,
            onCheckedChange = { onToggle() },
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFF80E5FF).copy(alpha = 0.5f),
                uncheckedThumbColor = Color(0xFF666666),
                uncheckedTrackColor = Color(0xFF222222),
            ),
        )
    }
}

/** Continue button — same as NameCardScreen's, kyant frosted glass. */
@Composable
private fun ContinueButton(
    enabled: Boolean,
    onClick: () -> Unit,
    backdrop: com.kyant.backdrop.backdrops.LayerBackdrop,
    modifier: Modifier = Modifier,
) {
    val interaction = remember { MutableInteractionSource() }
    val scale = if (enabled) 1f else 1f
    val overlayAlpha = if (enabled) 0.85f else 0.5f
    val overlayColor = if (enabled) Color.White else Color(0xFF3A3A3A)
    val fg = if (enabled) Color.Black else Color(0xFF8E8E93)

    val shape = RoundedCornerShape(24.dp)
    Box(
        modifier = modifier
            .height(48.dp)
            .scale(scale)
            .clip(shape)
            .drawBackdrop(
                backdrop = backdrop,
                shape = { shape },
                effects = {
                    vibrancy()
                    colorControls(
                        brightness = if (enabled) 0.4f else 0.05f,
                        contrast = 1f,
                        saturation = 1.4f,
                    )
                    blur(16f.dp.toPx())
                },
                onDrawSurface = {
                    drawRect(overlayColor.copy(alpha = overlayAlpha))
                },
            )
            .clickable(
                enabled = enabled,
                interactionSource = interaction,
                indication = null,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "Continue",
            color = fg,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}
