package com.chrona.app.ui.clock

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ClockScreen(
    viewModel: ClockViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Analog clock face
        AnalogClock(
            hoursAngle = uiState.hoursAngle,
            minutesAngle = uiState.minutesAngle,
            secondsAngle = uiState.secondsAngle,
            modifier = Modifier.size(240.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Digital time
        Text(
            text = uiState.timeString,
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = uiState.dateString,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun AnalogClock(
    hoursAngle: Float,
    minutesAngle: Float,
    secondsAngle: Float,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val onSurface = MaterialTheme.colorScheme.onSurface
    val secondsColor = MaterialTheme.colorScheme.tertiary

    Canvas(modifier = modifier) {
        val radius = size.minDimension / 2f
        val center = Offset(size.width / 2f, size.height / 2f)

        // Clock face circle
        drawCircle(color = onSurface.copy(alpha = 0.05f), radius = radius)
        drawCircle(color = onSurface.copy(alpha = 0.15f), radius = radius, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4f))

        // Hour markers
        for (i in 0 until 12) {
            val angle = Math.toRadians((i * 30).toDouble())
            val markerLen = if (i % 3 == 0) radius * 0.15f else radius * 0.08f
            val start = Offset(
                center.x + (radius - markerLen) * sin(angle).toFloat(),
                center.y - (radius - markerLen) * cos(angle).toFloat()
            )
            val end = Offset(
                center.x + radius * 0.9f * sin(angle).toFloat(),
                center.y - radius * 0.9f * cos(angle).toFloat()
            )
            drawLine(color = onSurface.copy(alpha = 0.4f), start = start, end = end, strokeWidth = if (i % 3 == 0) 4f else 2f)
        }

        // Hour hand
        drawHand(center, radius * 0.5f, hoursAngle, primaryColor, 10f)
        // Minute hand
        drawHand(center, radius * 0.7f, minutesAngle, onSurface, 6f)
        // Second hand
        drawHand(center, radius * 0.8f, secondsAngle, secondsColor, 3f)

        // Center dot
        drawCircle(color = primaryColor, radius = 10f, center = center)
    }
}

private fun DrawScope.drawHand(center: Offset, length: Float, angleDeg: Float, color: Color, strokeWidth: Float) {
    val angleRad = Math.toRadians(angleDeg.toDouble())
    val end = Offset(
        center.x + length * sin(angleRad).toFloat(),
        center.y - length * cos(angleRad).toFloat()
    )
    drawLine(color = color, start = center, end = end, strokeWidth = strokeWidth, cap = StrokeCap.Round)
}
