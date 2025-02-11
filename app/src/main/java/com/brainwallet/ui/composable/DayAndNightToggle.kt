package com.brainwallet.ui.composable

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.brainwallet.presenter.activities.intro.drawHalfCircle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.brainwallet.ui.screen.intro.ThemeViewModel


// toggle button
fun DrawScope.drawHalfCircle(rotation: Float) {
    rotate(rotation) {
        // black half circle
        drawArc(
            color = Color.Black,
            startAngle = -90f,
            sweepAngle = 180f,
            useCenter = true,
            size = Size(size.width, size.height),
            topLeft = Offset.Zero
        )
        // white half circle
        drawArc(
            color = Color.White,
            startAngle = 90f,
            sweepAngle = 180f,
            useCenter = true,
            size = Size(size.width, size.height),
            topLeft = Offset.Zero
        )
    }
}

@Composable
fun SettingsDayAndNight(viewModel: ThemeViewModel = viewModel()) {
    val isDarkMode by viewModel.isDarkMode.collectAsState(initial = false)
    val context = LocalContext.current // ✅ Get context in Composable

    // Rotation animation based on isDarkMode
    val rotation by animateFloatAsState(
        targetValue = if (isDarkMode) 0f else 180f,
        label = ""
    )

    Canvas(
        modifier = Modifier
            .size(48.dp)
            .border(1.dp, color = if (isDarkMode) Color.White else Color.Black, RoundedCornerShape(30.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() }, // No ripple effect
                indication = null
            ) {
                viewModel.toggleTheme() // ✅ Corrected toggle logic
            }
    ) {
        drawHalfCircle(rotation)
    }
}