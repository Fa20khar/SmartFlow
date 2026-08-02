package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlin.math.cos
import kotlin.math.sin

/**
 * Unique SmartFlow Multi-Orbit Pulse Loader
 * A mesmerizing, high-tech animated loader featuring rotating document store orbits,
 * pulsing glowing nodes, and real-time MongoDB transaction text.
 */
@Composable
fun UniqueSmartFlowLoader(
    modifier: Modifier = Modifier,
    size: Dp = 120.dp,
    title: String = "MongoDB Document Sync",
    subtitle: String = "Verifying SmartFlow 3-Way Audit Trail..."
) {
    val infiniteTransition = rememberInfiniteTransition(label = "loader_transition")

    // Infinite rotation for outer orbit
    val outerRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "outer_rotation"
    )

    // Reverse rotation for inner orbit
    val innerRotation by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "inner_rotation"
    )

    // Pulsing scale for center core
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    // Alpha glow pulse
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    val emeraldColor = Color(0xFF13AA52)
    val tealAccent = Color(0xFF00ED64)
    val cyanGlow = Color(0xFF00E5FF)
    val deepDarkBg = Color(0xFF022320)

    Column(
        modifier = modifier
            .testTag("unique_smart_flow_loader"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.size(size),
            contentAlignment = Alignment.Center
        ) {
            // Outer glowing background ring
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(this.size.width / 2, this.size.height / 2)
                val radius = (this.size.minDimension / 2) - 12.dp.toPx()

                // Glow background ring
                drawCircle(
                    color = emeraldColor.copy(alpha = 0.12f * glowAlpha),
                    radius = radius + 8.dp.toPx()
                )

                // Outer dashed track
                drawCircle(
                    color = Color.White.copy(alpha = 0.15f),
                    radius = radius,
                    style = Stroke(width = 2.dp.toPx())
                )
            }

            // Outer Rotating Arc & Satellites
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .rotate(outerRotation)
            ) {
                val center = Offset(this.size.width / 2, this.size.height / 2)
                val radius = (this.size.minDimension / 2) - 12.dp.toPx()

                // Primary Gradient Arc
                drawArc(
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            emeraldColor,
                            tealAccent,
                            cyanGlow,
                            Color.Transparent
                        )
                    ),
                    startAngle = 0f,
                    sweepAngle = 270f,
                    useCenter = false,
                    style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                )

                // Orbiting satellite dots
                val angleRad1 = Math.toRadians(0.0)
                val dot1X = (center.x + radius * cos(angleRad1)).toFloat()
                val dot1Y = (center.y + radius * sin(angleRad1)).toFloat()
                drawCircle(
                    color = tealAccent,
                    radius = 5.dp.toPx(),
                    center = Offset(dot1X, dot1Y)
                )

                val angleRad2 = Math.toRadians(180.0)
                val dot2X = (center.x + radius * cos(angleRad2)).toFloat()
                val dot2Y = (center.y + radius * sin(angleRad2)).toFloat()
                drawCircle(
                    color = cyanGlow,
                    radius = 4.dp.toPx(),
                    center = Offset(dot2X, dot2Y)
                )
            }

            // Inner Reverse Counter-Rotating Ring
            Canvas(
                modifier = Modifier
                    .size(size * 0.68f)
                    .rotate(innerRotation)
            ) {
                val center = Offset(this.size.width / 2, this.size.height / 2)
                val radius = (this.size.minDimension / 2) - 6.dp.toPx()

                drawArc(
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            cyanGlow,
                            emeraldColor,
                            Color.Transparent
                        )
                    ),
                    startAngle = 90f,
                    sweepAngle = 220f,
                    useCenter = false,
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            // Center Pulsing Core with Database Icon
            Box(
                modifier = Modifier
                    .size(size * 0.42f)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                emeraldColor.copy(alpha = glowAlpha),
                                deepDarkBg
                            )
                        )
                    )
                    .border(1.5.dp, tealAccent.copy(alpha = glowAlpha), CircleShape)
                    .shadow(elevation = 8.dp, shape = CircleShape, spotColor = emeraldColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Storage,
                    contentDescription = "DB Syncing Core",
                    tint = Color.White,
                    modifier = Modifier.size(size * 0.22f)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                letterSpacing = 0.5.sp
            ),
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(emeraldColor)
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp
            )
        }
    }
}

/**
 * Fullscreen / Dialog Overlay Unique Loader
 */
@Composable
fun UniqueSmartFlowLoaderDialog(
    onDismiss: () -> Unit = {},
    title: String = "MongoDB Cluster Sync",
    subtitle: String = "Processing SmartFlow Enterprise Ledger..."
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF022B28)),
            elevation = CardDefaults.cardElevation(defaultElevation = 16.dp),
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(16.dp)
                .border(1.5.dp, Color(0xFF13AA52).copy(alpha = 0.6f), RoundedCornerShape(24.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                UniqueSmartFlowLoader(
                    size = 110.dp,
                    title = title,
                    subtitle = subtitle
                )

                Spacer(modifier = Modifier.height(20.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color.White.copy(alpha = 0.1f))
                ) {
                    val infiniteTransition = rememberInfiniteTransition(label = "bar_transition")
                    val progressX by infiniteTransition.animateFloat(
                        initialValue = -0.3f,
                        targetValue = 1.3f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1500, easing = LinearOutSlowInEasing),
                            repeatMode = RepeatMode.Restart
                        ),
                        label = "progress_x"
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(0.35f)
                            .align(Alignment.CenterStart)
                            .offset(x = (progressX * 250).dp)
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        Color.Transparent,
                                        Color(0xFF00ED64),
                                        Color.Transparent
                                    )
                                )
                            )
                    )
                }
            }
        }
    }
}
