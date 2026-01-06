package sim2.app.talleb_5edma.screens

import android.graphics.BitmapFactory
import android.graphics.SurfaceTexture
import android.util.Base64
import android.view.Surface
import android.view.TextureView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import sim2.app.talleb_5edma.network.WebSocketCallManager
import sim2.app.talleb_5edma.R
import sim2.app.talleb_5edma.Routes
import sim2.app.talleb_5edma.util.BASE_URL
import java.io.ByteArrayInputStream

// Define common colors
private val ControlBgColor = Color.Black.copy(alpha = 0.35f)
private val HangUpColor = Color(0xFFFE294D)

@Composable
fun CallScreenUI(
    isVideoCall: Boolean,
    toUserName: String,
    toUserImage: String? = null, // Add this parameter for user image
    // UI State
    callStatusText: String,
    isVideoStreaming: Boolean,
    isAudioStreaming: Boolean,
    showRemoteVideo: Boolean,
    remoteBitmap: ImageBitmap?,
    // Callbacks
    onChatClick: () -> Unit,
    onVideoToggle: () -> Unit,
    onCameraSwitch: () -> Unit,
    onAudioToggle: () -> Unit,
    onHangUp: () -> Unit,
    selfViewContent: @Composable (() -> Unit)? = null
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Background
        if (remoteBitmap != null && isVideoCall && showRemoteVideo) {
            Image(
                bitmap = remoteBitmap,
                contentDescription = "Remote video feed",
                modifier = Modifier
                    .fillMaxSize()
                    .blur(radius = 0.dp),
                contentScale = ContentScale.Crop
            )
        } else {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Call background",
                modifier = Modifier
                    .fillMaxSize()
                    .blur(radius = if (isVideoCall && showRemoteVideo) 0.dp else 25.dp),
                contentScale = ContentScale.Crop
            )
        }

        // Dark overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha =
                    if (isVideoCall && showRemoteVideo && remoteBitmap != null) 0.0f
                    else 0.5f
                ))
        )

        // Self View - Now positioned at top right by default
        selfViewContent?.invoke()

        // Main Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 44.dp)
                .systemBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                CallControlButton(
                    icon = Icons.Filled.ChatBubble,
                    onClick = onChatClick,
                    backgroundColor = ControlBgColor,
                    contentColor = Color.White,
                    buttonSize = 48.dp,
                    iconSize = 24.dp
                )
            }

            // Central User Info
            if (!isVideoCall || !showRemoteVideo || remoteBitmap == null) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Updated Avatar with user image
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!toUserImage.isNullOrEmpty()) {
                            val imageUrl = "$BASE_URL/${toUserImage}"
                            AsyncImage(
                                model = imageUrl,
                                contentDescription = "User Avatar",
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            // Fallback to icon (similar to your first screen)
                            Icon(
                                Icons.Default.AccountCircle,
                                contentDescription = "Avatar",
                                tint = Color.White,
                                modifier = Modifier.size(70.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Name
                    Text(
                        text = toUserName,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 24.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Status
                    Text(
                        text = callStatusText,
                        color = Color.White.copy(alpha = 0.6f),
                        fontWeight = FontWeight.Normal,
                        fontSize = 17.sp,
                        letterSpacing = (-0.33).sp,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
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
                // Camera Toggle
                if (isVideoCall) {
                    CallControlButton(
                        icon = if (isVideoStreaming) Icons.Default.VideocamOff else Icons.Default.Videocam,
                        onClick = onVideoToggle,
                        backgroundColor = if (isVideoStreaming) Color.White else ControlBgColor,
                        contentColor = if (isVideoStreaming) Color.Black else Color.White,
                        buttonSize = 64.dp,
                        iconSize = 32.dp
                    )
                }

                // Camera Switch
                if (isVideoCall && isVideoStreaming) {
                    CallControlButton(
                        icon = Icons.Default.Cameraswitch,
                        onClick = onCameraSwitch,
                        backgroundColor = ControlBgColor,
                        contentColor = Color.White,
                        buttonSize = 64.dp,
                        iconSize = 32.dp
                    )
                }

                // Microphone Toggle
                CallControlButton(
                    icon = if (isAudioStreaming) Icons.Default.Mic else Icons.Default.MicOff,
                    onClick = onAudioToggle,
                    backgroundColor = if (isAudioStreaming) ControlBgColor else Color.White,
                    contentColor = if (isAudioStreaming) Color.White else Color.Black,
                    buttonSize = 64.dp,
                    iconSize = 32.dp
                )

                // Hang up
                CallControlButton(
                    icon = Icons.Default.CallEnd,
                    onClick = onHangUp,
                    backgroundColor = HangUpColor,
                    contentColor = Color.White,
                    buttonSize = 64.dp,
                    iconSize = 32.dp
                )
            }
        }
    }
}

// Usage example with state management
@Composable
fun ScreenCall(
    isVideoCall: Boolean,
    navController: NavController,
    toUserId: String = "",
    callManager: WebSocketCallManager,
    chatId: String = ""
) {
    val coroutineScope = rememberCoroutineScope()
    val screenWidth = LocalWindowInfo.current.containerSize.width.dp

    // Collect state from call manager
    val callState by callManager.callState.collectAsState()
    val isVideoStreaming by callManager.isVideoStreaming.collectAsState()
    val isAudioStreaming by callManager.isAudioStreaming.collectAsState()
    val remoteVideoFrame by callManager.remoteVideoFrame.collectAsState()

    val toUserName = callManager.toUserName
    val toUserImage = callManager.toUserImage


    // Convert remote video frame to bitmap
    val remoteBitmap = remember(remoteVideoFrame) {
        remoteVideoFrame?.let { frameData ->
            try {
                val imageBytes = Base64.decode(frameData, Base64.DEFAULT)
                BitmapFactory.decodeStream(ByteArrayInputStream(imageBytes))?.asImageBitmap()
            } catch (_: Exception) {
                null
            }
        }
    }

    // Get status text
    val callStatusText = getCallStatusText(callState)

    // Initialize call when screen starts
    LaunchedEffect(Unit) {
        if (callState is WebSocketCallManager.CallState.Idle && toUserId.isNotEmpty()) {
            callManager.makeCall(toUserId, isVideoCall, chatId)
        }
    }

    // Handle call state changes
    LaunchedEffect(callState) {
        when (callState) {
            is WebSocketCallManager.CallState.CallFailed,
            is WebSocketCallManager.CallState.Idle -> {
                navController.popBackStack()
            }
            else -> {}
        }
    }

    CallScreenUI(
        isVideoCall = isVideoCall,
        toUserName = toUserName,
        toUserImage = toUserImage, // Pass the user image to the UI
        callStatusText = callStatusText,
        isVideoStreaming = isVideoStreaming,
        isAudioStreaming = isAudioStreaming,
        showRemoteVideo = isVideoStreaming,
        remoteBitmap = remoteBitmap,
        onChatClick = {
            navController.navigate(Routes.ScreenChating)
        },
        onVideoToggle = {
            coroutineScope.launch {
                callManager.toggleVideo()
            }
        },
        onCameraSwitch = {
            coroutineScope.launch {
                callManager.switchCamera()
            }
        },
        onAudioToggle = {
            coroutineScope.launch {
                callManager.toggleAudio()
            }
        },
        onHangUp = {
            coroutineScope.launch {
                callManager.hangUp()
            }
        },
        selfViewContent = if (isVideoCall && isVideoStreaming) {
            {
                DraggableSelfViewPreview(
                    callManager = callManager,
                    screenWidth = screenWidth
                )
            }
        } else null
    )
}

@Composable
fun DraggableSelfViewPreview(
    callManager: WebSocketCallManager,
    screenWidth: Dp
) {
    var offset by remember { mutableStateOf(Offset.Zero) }

    // Calculate initial position for top right corner
    val initialX = screenWidth /2 + 170.dp
    val initialY = 76.dp

    // Initialize offset on first composition
    LaunchedEffect(Unit) {
        offset = Offset(initialX.value, initialY.value)
    }

    Box(
        modifier = Modifier
            .offset { IntOffset(offset.x.toInt(), offset.y.toInt()) }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        val newOffset = Offset(
                            offset.x + dragAmount.x,
                            offset.y + dragAmount.y
                        )
                        offset = newOffset
                        println("Dragged to: $newOffset")
                    },
                    onDragEnd = { }
                )
            }
    ) {
        SelfViewPreview(
            modifier = Modifier,
            callManager = callManager
        )
    }
}

@Composable
fun SelfViewPreview(
    modifier: Modifier = Modifier,
    callManager: WebSocketCallManager
) {
    Box(
        modifier = modifier
            .size(width = 120.dp, height = 160.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.Black)
            .border(2.dp, Color(0xFF8C8C8C), RoundedCornerShape(8.dp))
    ) {
        AndroidView(
            factory = { ctx ->
                TextureView(ctx).apply {
                    surfaceTextureListener = object : TextureView.SurfaceTextureListener {
                        override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
                            val previewSurface = Surface(surface)
                            callManager.mediaStreamManager?.enableLocalPreview(previewSurface)
                        }

                        override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {}

                        override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
                            callManager.mediaStreamManager?.disableLocalPreview()
                            return true
                        }

                        override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}
                    }
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(8.dp))
        )
    }
}

@Composable
fun CallControlButton(
    icon: ImageVector,
    onClick: () -> Unit,
    backgroundColor: Color,
    contentColor: Color,
    buttonSize: Dp,
    iconSize: Dp
) {
    val interactionSource = remember { MutableInteractionSource() }

    Surface(
        modifier = Modifier
            .size(buttonSize)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        shape = CircleShape,
        color = backgroundColor,
        contentColor = contentColor
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(iconSize)
            )
        }
    }
}

// Helper function to get status text based on call state
private fun getCallStatusText(callState: WebSocketCallManager.CallState): String {
    return when (callState) {
        is WebSocketCallManager.CallState.Connecting -> "Connecting..."
        is WebSocketCallManager.CallState.OutgoingCall -> "Calling..."
        is WebSocketCallManager.CallState.IncomingCall -> "Incoming call"
        is WebSocketCallManager.CallState.InCall -> "Connected"
        is WebSocketCallManager.CallState.CallFailed -> "Call failed"
        else -> "Contacting..."
    }
}

// Preview with sample state
@Preview(showBackground = true)
@Composable
fun PreviewCallScreenUI() {
    CallScreenUI(
        isVideoCall = true,
        toUserName = "Martha Craig",
        toUserImage = "path/to/image.jpg", // Add sample image for preview
        callStatusText = "00:45",
        isVideoStreaming = true,
        isAudioStreaming = true,
        showRemoteVideo = true,
        remoteBitmap = null,
        onChatClick = {},
        onVideoToggle = {},
        onCameraSwitch = {},
        onAudioToggle = {},
        onHangUp = {},
        selfViewContent = null
    )
}

@Preview(showBackground = true, name = "Audio Call")
@Composable
fun PreviewCallScreenUIAudio() {
    CallScreenUI(
        isVideoCall = false,
        toUserName = "John Doe",
        toUserImage = null, // Test with no image
        callStatusText = "Calling...",
        isVideoStreaming = false,
        isAudioStreaming = true,
        showRemoteVideo = false,
        remoteBitmap = null,
        onChatClick = {},
        onVideoToggle = {},
        onCameraSwitch = {},
        onAudioToggle = {},
        onHangUp = {},
        selfViewContent = null
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewScreenCall() {
    val context = LocalContext.current
    val callManager = remember { WebSocketCallManager(context) }
    ScreenCall(
        isVideoCall = false,
        navController = rememberNavController(),
        toUserId = "user123",
        callManager = callManager
    )
}

@Preview(showBackground = true, name = "Video On State")
@Composable
fun PreviewScreenCallVideoOn() {
    val context = LocalContext.current
    val callManager = remember { WebSocketCallManager(context) }
    ScreenCall(
        isVideoCall = true,
        navController = rememberNavController(),
        toUserId = "user123",
        callManager = callManager
    )
}