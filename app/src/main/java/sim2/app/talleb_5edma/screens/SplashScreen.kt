package sim2.app.talleb_5edma.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import sim2.app.talleb_5edma.R
import sim2.app.talleb_5edma.Routes

@Composable
fun SplashScreen(
    navController: NavController,
    startDestination: String
) {
    val scaleAnim = remember { Animatable(0f) }
    val rotationAnim = remember { Animatable(0f) }
    val alphaAnim = remember { Animatable(0f) }
    
    // Breathing animation for "loading" effect
    val infiniteTransition = rememberInfiniteTransition(label = "breathing")
    val breathingScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    LaunchedEffect(Unit) {
        // Parallel animations
        launch {
            scaleAnim.animateTo(
                targetValue = 1f,
                animationSpec = androidx.compose.animation.core.spring(
                    dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
                    stiffness = androidx.compose.animation.core.Spring.StiffnessLow
                )
            )
        }
        
        launch {
            alphaAnim.animateTo(
                targetValue = 1f,
                animationSpec = tween(500)
            )
        }

        launch {
            rotationAnim.animateTo(
                targetValue = 360f, // 1 spin
                animationSpec = tween(
                    durationMillis = 1200,
                    easing = FastOutSlowInEasing
                )
            )
        }
        
        // Ensure we stay for at least 3 seconds total to show off the effect
        delay(3000)

        // Navigate
        navController.navigate(startDestination) {
            popUpTo(Routes.Splash) { inclusive = true }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White), // Fallback
        contentAlignment = Alignment.Center
    ) {
        // Background Image
        Image(
            painter = painterResource(id = R.drawable.back),
            contentDescription = "Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = androidx.compose.ui.layout.ContentScale.Crop
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Logo with 3D flip and Entrance Scale
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Logo",
                modifier = Modifier
                    .size(200.dp)
                    .graphicsLayer {
                        alpha = alphaAnim.value
                        // Combine entrance scale with breathing scale
                        val currentScale = scaleAnim.value * if(scaleAnim.value > 0.9f) breathingScale else 1f
                        scaleX = currentScale
                        scaleY = currentScale
                        rotationY = rotationAnim.value
                        cameraDistance = 12f * density
                    }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Shadow
            Box(
                modifier = Modifier
                    .size(width = 140.dp, height = 20.dp)
                    .graphicsLayer {
                        alpha = alphaAnim.value * 0.3f // Semi-transparent
                        scaleX = scaleAnim.value * if(scaleAnim.value > 0.9f) (2f - breathingScale) else 1f // Shadow shrinks when logo grows (lifting effect)
                    }
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(Color.Black, Color.Transparent),
                            radius = 100f
                        )
                    )
            )
        }
    }
}
