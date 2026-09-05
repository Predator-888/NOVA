package com.example.nova.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun IntroScreen(onTimeout: () -> Unit) {
    // State to trigger the animation
    var startAnimation by remember { mutableStateOf(false) }

    // The alpha value will smoothly transition from 0f (invisible) to 1f (fully visible)
    val alphaAnim by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 1500), // Takes 1.5 seconds to fade in
        label = "fade_in_animation"
    )

    // This block runs exactly once when the screen opens
    LaunchedEffect(key1 = true) {
        startAnimation = true // Trigger the fade-in
        delay(2500)           // Keep the screen visible for 2.5 seconds total
        onTimeout()           // Tell MainActivity to switch to the AppRoot
    }

    // The UI for the Splash Screen
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "NOVA",
            fontSize = 56.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary, // Uses your emerald/accent color
            letterSpacing = 12.sp, // Spaces the letters out for a premium cinematic look
            modifier = Modifier.alpha(alphaAnim)
        )
    }
}