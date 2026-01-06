package sim2.app.talleb_5edma.screens

// NEW IMPORTS
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.remember
// ---
// NEW IMPORTS FOR STATE
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.shadow
// ---

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import sim2.app.talleb_5edma.R
import sim2.app.talleb_5edma.Routes

// Define common colors/modifiers
private val ControlBgColor = Color.Black.copy(alpha = 0.35f)
private val HangUpColor = Color(0xFFFE294D)

@Composable
fun ScreenCall(isVideoCall: Boolean, navController: NavController) {
    // --- STATE VARIABLES ---
    // These variables remember the state of the call controls
    var isVideoOn by remember { mutableStateOf(isVideoCall) }
    var isMicOn by remember { mutableStateOf(true) }
    var isSpeakerOn by remember { mutableStateOf(true) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black) // Base background
    ) {
        // --- 1. Background Image & Scrim (Rectangle) ---
        Image(
            painter = painterResource(id = R.drawable.img),
            contentDescription = "Caller Video Feed",
            modifier = Modifier
                .fillMaxSize()
                // CHANGED: Blur is now conditional
                .blur(radius = if (isVideoOn) 0.dp else 25.dp),
            contentScale = ContentScale.Crop
        )

        // Dark scrim overlay to make text readable
        Box(
            modifier = Modifier
                .fillMaxSize()
                // CHANGED: Scrim is now conditional
                .background(Color.Black.copy(alpha = if (isVideoOn) 0.0f else 0.5f))
        )

        // --- 2. Status Bar (iPhone X / Dark) ---
        CallStatusBar()

        // --- 3. Self View (Your own camera) ---
        // This appears when video is on, aligned to TopEnd
        if (isVideoOn) {
            SelfViewPreview(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 60.dp, end = 20.dp) // Positioned below status bar
            )
        }

        // --- 4. UI Content ---
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 44.dp) // Below status bar
                .systemBarsPadding(), // Ensures content respects system bars
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Controls (Chat Button)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                CallControlButton(
                    icon = Icons.Filled.ChatBubble,
                    onClick = {
                        navController.navigate(Routes.ScreenChating)
                    },
                    backgroundColor = ControlBgColor,
                    contentColor = Color.White, // Icon color
                    buttonSize = 48.dp,
                    iconSize = 24.dp
                )
            }

            // Central User Info
            // CHANGED: This entire block is now conditional
            if (!isVideoOn) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Avatar (Oval)
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(Color.Gray), // Placeholder
                        contentAlignment = Alignment.Center
                    ) {
                        Text("MC", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Name (Martha Craig)
                    Text(
                        text = "Martha Craig",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 24.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Status (Contacting…)
                    Text(
                        // CHANGED: Text updates based on video state
                        text = if (isVideoOn) "0:10" else "Contacting…",
                        color = Color.White.copy(alpha = 0.6f),
                        fontWeight = FontWeight.Normal,
                        fontSize = 17.sp,
                        letterSpacing = (-0.33).sp,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                // Spacer to push controls down when video is on
                Spacer(modifier = Modifier.weight(1f))
            }


            // Bottom Controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 34.dp)
                    .padding(bottom = 40.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                // 2. Camera Flip/Video Toggle
                CallControlButton(
                    // CHANGED: Icon toggles
                    icon = if (isVideoOn) Icons.Default.VideocamOff else Icons.Default.Videocam,
                    onClick = { isVideoOn = !isVideoOn }, // Toggle state
                    // CHANGED: Colors toggle
                    backgroundColor = if (isVideoOn) Color.White else ControlBgColor,
                    contentColor = if (isVideoOn) Color.Black else Color.White,
                    buttonSize = 64.dp,
                    iconSize = 32.dp
                )

                // 1. Sound/Speaker Toggle
                CallControlButton(
                    // CHANGED: Icon toggles
                    icon = if (isSpeakerOn) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeOff,
                    onClick = { isSpeakerOn = !isSpeakerOn }, // Toggle state
                    // CHANGED: Colors toggle
                    backgroundColor = if (isSpeakerOn) ControlBgColor else Color.White,
                    contentColor = if (isSpeakerOn) Color.White else Color.Black,
                    buttonSize = 64.dp,
                    iconSize = 32.dp
                )

                // 3. Microphone Toggle
                CallControlButton(
                    // CHANGED: Icon toggles
                    icon = if (isMicOn) Icons.Default.Mic else Icons.Default.MicOff,
                    onClick = { isMicOn = !isMicOn }, // Toggle state
                    // CHANGED: Colors toggle
                    backgroundColor = if (isMicOn) ControlBgColor else Color.White,
                    contentColor = if (isMicOn) Color.White else Color.Black,
                    buttonSize = 64.dp,
                    iconSize = 32.dp
                )

                // 4. Hang up
                CallControlButton(
                    icon = Icons.Default.CallEnd,
                    onClick = { /* TODO: End Call */ },
                    backgroundColor = HangUpColor,
                    contentColor = Color.White,
                    buttonSize = 64.dp,
                    iconSize = 32.dp
                )
            }
        }
    }
}

// --- THIS COMPOSABLE IS UPDATED ---
@Composable
fun CallControlButton(
    icon: ImageVector,
    onClick: () -> Unit,
    backgroundColor: Color,
    contentColor: Color, // CHANGED: Added contentColor parameter
    buttonSize: Dp,
    iconSize: Dp
) {
    val interactionSource = remember { MutableInteractionSource() }

    Surface(
        modifier = Modifier
            .size(buttonSize)
            .clickable(
                interactionSource = interactionSource,
                indication = null, // Disables the ripple/flash effect
                onClick = onClick
            ),
        shape = CircleShape,
        color = backgroundColor,
        contentColor = contentColor // CHANGED: Use the parameter here
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = null, // Describe for accessibility
                modifier = Modifier.size(iconSize)
                // Icon will automatically use the Surface's contentColor
            )
        }
    }
}

// --- NEW COMPOSABLE FOR SELF VIEW ---
@Composable
fun SelfViewPreview(modifier: Modifier = Modifier) {
    // This is the "image 6" from your Figma spec
    Box(
        modifier = modifier
            .size(width = 131.dp, height = 197.dp)
            .clip(RoundedCornerShape(5.dp))
            .background(Color.Gray) // Placeholder for self camera feed
            .border(2.dp, Color(0xFF8C8C8C), RoundedCornerShape(5.dp))
            .shadow(4.dp, RoundedCornerShape(5.dp), clip = false)
    ) {
        // You would put your actual camera preview here
        // For this preview, we can just add a placeholder icon
        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = "Self View",
            tint = Color.White.copy(alpha = 0.8f),
            modifier = Modifier
                .size(80.dp)
                .align(Alignment.Center)
        )
    }
}


// Minimalist Status Bar approximation
@Composable
fun CallStatusBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .padding(horizontal = 21.dp)
            .zIndex(1f), // Ensure status bar is on top
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "9:41",
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
            fontSize = 15.sp,
            letterSpacing = (-0.3).sp
        )
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Icon(Icons.Default.SignalCellularAlt, contentDescription = "Cellular", tint = Color.White, modifier = Modifier.size(17.dp))
            Icon(Icons.Default.Wifi, contentDescription = "WiFi", tint = Color.White, modifier = Modifier.size(17.dp))
            Icon(Icons.Default.BatteryFull, contentDescription = "Battery", tint = Color.White, modifier = Modifier.size(24.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewScreenCall() {
    ScreenCall(false, rememberNavController())
}

// --- ADDED A PREVIEW for the Video-On state ---
@Preview(showBackground = true, name = "Video On State")
@Composable
fun PreviewScreenCallVideoOn() {
    // This composable isn't real, but we can simulate the state
    // by manually building the view with isVideoOn = true.
    // For a real app, you'd just run the app and click the button.
    // This is a simplified preview.
    Box(modifier = Modifier.fillMaxSize()) {
        // Since we can't easily pass state to a @Preview,
        // this preview just shows the SelfViewPreview composable.
        SelfViewPreview(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 60.dp, end = 20.dp)
        )
    }
}