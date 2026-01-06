package sim2.app.talleb_5edma.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import sim2.app.talleb_5edma.R
import sim2.app.talleb_5edma.Routes

// --- Data Models for the Chat ---

// A sealed class to represent all possible items in the chat list
sealed class ChatItem {
    data class Message(
        val id: Int,
        val text: String,
        val author: String,
        val isSent: Boolean,
        val showAvatar: Boolean = false
    ) : ChatItem()

    data class Image(
        val id: Int,
        val imageUrl: String, // In a real app, use a URI/URL
        val isSent: Boolean,
        val showAvatar: Boolean = false
    ) : ChatItem()

    data class Voice(
        val id: Int,
        val waveUrl: String, // Placeholder
        val isSent: Boolean
    ) : ChatItem()

    data class DateSeparator(val id: Int, val date: String) : ChatItem()
}

// Dummy data list based on the CSS layout description
val dummyChatItems = listOf(
    ChatItem.DateSeparator(1, "21:32"),
    ChatItem.Voice(2, "wave_url", isSent = true),
    ChatItem.DateSeparator(3, "16:44"),
    ChatItem.Message(4, "Salut Mr", "Bâtiments Plus", isSent = false, showAvatar = true),
    ChatItem.Message(5, "How are you doing?", "Bâtiments Plus", isSent = false, showAvatar = false),
    ChatItem.DateSeparator(6, "16:44"),
    ChatItem.Message(7, "Je suis intéressé par le poste Assistant de chantier.", "Me", isSent = true),
    ChatItem.Message(8, "Une formation est-elle prévue ?", "Me", isSent = true),
    ChatItem.DateSeparator(9, "16:44"),
    ChatItem.Message(10, "Oui, absolument.", "Bâtiments Plus", isSent = false, showAvatar = true),
    ChatItem.Image(11, "image_url", isSent = false, showAvatar = false),
    ChatItem.Message(12, "How are you doing?", "Bâtiments Plus", isSent = false, showAvatar = false),
)

// --- Main Screen Composable ---

@Composable
fun ScreenChating(navController: NavController = rememberNavController()) {
    Scaffold(
        topBar = { ChatTopBar(navController) },
        bottomBar = { ChatBottomBar() }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            reverseLayout = true
        ) {
            items(dummyChatItems.reversed()) { item ->
                when (item) {
                    is ChatItem.DateSeparator -> DateSeparator(item.date)
                    is ChatItem.Message -> ChatMessageItem(item)
                    is ChatItem.Image -> ChatImageItem(item)
                    is ChatItem.Voice -> ChatVoiceItem(item)
                }
            }
        }
    }
}
// --- Reusable Chat Item Composables ---

@Composable
fun ChatMessageItem(item: ChatItem.Message) {
    val horizontalArrangement = if (item.isSent) Arrangement.End else Arrangement.Start
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = horizontalArrangement,
        verticalAlignment = Alignment.Bottom
    ) {
        if (item.showAvatar) {
            // R.drawable.avatar_placeholder should be replaced with a real resource
            // Image(
            //     painter = painterResource(id = R.drawable.avatar_placeholder),
            //     contentDescription = "Avatar",
            //     modifier = Modifier.size(28.dp).clip(CircleShape).background(Color.Gray)
            // )
            Box(Modifier.size(28.dp).clip(CircleShape).background(Color.LightGray))
            {
                Icon(Icons.Default.AccountCircle, "Voice message", modifier = Modifier.fillMaxSize())
            }
            Spacer(Modifier.width(8.dp))
        } else if (!item.isSent) {
            // Spacer to align non-avatar messages
            Spacer(Modifier.width(36.dp)) 
        }

        MessageBubble(
            text = item.text,
            isSent = item.isSent,
            modifier = Modifier.weight(1f, fill = false) // Bubble wraps content
        )
    }
}

@Composable
fun ChatImageItem(item: ChatItem.Image) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (item.isSent) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        if (item.showAvatar) {
            Box(Modifier.size(28.dp).clip(CircleShape).background(Color.LightGray))
            Spacer(Modifier.width(8.dp))
        } else if (!item.isSent) {
            Spacer(Modifier.width(36.dp))
        }

        // Placeholder for the image from CSS
        Box(
            modifier = Modifier
                .width(230.dp)
                .height(116.dp)
                .clip(RoundedCornerShape(topStart = 15.dp, topEnd = 18.dp, bottomEnd = 18.dp, bottomStart = 0.dp))
                .background(Color.Gray), // Placeholder for image
            contentAlignment = Alignment.Center
        ) {
            // Play button "Subtract"
            Box(
                modifier = Modifier
                    .size(45.dp)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = Color.Black)
            }
        }
    }
}

@Composable
fun ChatVoiceItem(item: ChatItem.Voice) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (item.isSent) Arrangement.End else Arrangement.Start
    ) {
        // "Wave" from CSS
        Box(
            modifier = Modifier
                .size(83.dp)
                .clip(CircleShape)
                .background(Color.LightGray), // Placeholder for "Wave"
            contentAlignment = Alignment.Center
        ) {
            Image(painter = painterResource(R.drawable.hello), "Voice message", modifier = Modifier.fillMaxSize())
        }
    }
}

@Composable
fun MessageBubble(text: String, isSent: Boolean, modifier: Modifier = Modifier) {
    // Sent bubble shape: 18px 18px 4px 18px
    val sentShape = RoundedCornerShape(
        topStart = 18.dp,
        topEnd = 18.dp,
        bottomEnd = 4.dp,
        bottomStart = 18.dp
    )
    // Received bubble shape: 4px 18px 18px 18px
    val receivedShape = RoundedCornerShape(
        topStart = 4.dp,
        topEnd = 18.dp,
        bottomEnd = 18.dp,
        bottomStart = 18.dp
    )
    
    val bubbleColor = Color.Black.copy(alpha = 0.06f) // CSS color

    Surface(
        modifier = modifier,
        shape = if (isSent) sentShape else receivedShape,
        color = bubbleColor
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            fontSize = 17.sp,
            fontFamily = FontFamily.Serif, // 'Source Serif Pro'
            color = Color.Black
        )
    }
}

@Composable
fun DateSeparator(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black.copy(alpha = 0.3f) // rgba(0, 0, 0, 0.3)
        )
    }
}

// --- Top and Bottom Bar Composables ---

@Composable
fun ChatTopBar(navController: NavController) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(84.dp)
            .background(Color(0xFF920000)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = {
            navController.navigate(Routes.ScreenOffre)
        }) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.White
            )
        }

        Box(
            Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(Color.LightGray)
        )

        Spacer(Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Bâtiments Plus",
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                fontFamily = FontFamily.Default
            )
            Text(
                text = "En ligne 14 h ago",
                color = Color(0xFFAEAEAE),
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                fontFamily = FontFamily.Default
            )
        }

        // Voice call button
        IconButton(onClick = {
            // Navigate to call screen with voice call (video false)
            navController.navigate(Routes.ScreenCall + "/false")
        }) {
            Icon(
                imageVector = Icons.Default.Call,
                contentDescription = "Voice Call",
                tint = Color.White,
                modifier = Modifier.size(54.dp)
            )
        }

        // Video call button
        IconButton(onClick = {
            // Navigate to call screen with video call (video true)
            navController.navigate(Routes.ScreenCall + "/false")
        }) {
            Icon(
                imageVector = Icons.Default.Videocam,
                contentDescription = "Video Call",
                tint = Color.White,
                modifier = Modifier.size(54.dp)
            )
        }
        Spacer(Modifier.width(10.dp))
    }
}

@Composable
fun ChatBottomBar() {
    var text by remember { mutableStateOf("") }

    // "Send Message" container
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(59.dp),
        color = Color.White.copy(alpha = 0.6f),
        border = BorderStroke(0.5.dp, Color(0xFFA3A3A3)) // border-top
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // "Actions"
            IconButton(onClick = { /* TODO */ }) {
                Icon(Icons.Default.AddCircle, "Actions", tint = Color(0xFFFF5151))
            }
            // "Photo"
            IconButton(onClick = { /* TODO */ }) {
                Icon(Icons.Default.PhotoCamera, "Photo", tint = Color(0xFFFF5151))
            }
            // "Audio"
            IconButton(onClick = { /* TODO */ }) {
                Icon(Icons.Default.Mic, "Audio", tint = Color(0xFFFF5151))
            }

            // "Message Input"
            BasicTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier
                    .weight(1f)
                    .height(36.dp),
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                color = Color.Black.copy(alpha = 0.05f),
                                shape = RoundedCornerShape(18.dp)
                            )
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (text.isEmpty()) {
                            // "Aa" placeholder
                            Text(
                                text = "Aa",
                                color = Color(0xFF999999),
                                fontSize = 17.sp,
                                fontFamily = FontFamily.Default // 'Inter'
                            )
                        }
                        innerTextField()
                        // "Emoji"
                        Icon(
                            imageVector = Icons.Default.EmojiEmotions,
                            contentDescription = "Emoji",
                            tint = Color(0xFFFF5151),
                            modifier = Modifier.align(Alignment.CenterEnd)
                        )
                    }
                }
            )

            // "Like"
            IconButton(onClick = { /* TODO */ }) {
                Icon(Icons.Default.ThumbUp, "Like", tint = Color(0xFFFF5151))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ChatScreenPreview() {
    ScreenChating()
}