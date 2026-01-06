package sim2.app.talleb_5edma.screens

import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AllInclusive
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import sim2.app.talleb_5edma.network.WebSocketCallManager
import sim2.app.talleb_5edma.models.ChatListItem
import sim2.app.talleb_5edma.models.ChatMessage
import sim2.app.talleb_5edma.models.GetChatByIdResponse
import sim2.app.talleb_5edma.models.MessageType
import sim2.app.talleb_5edma.models.SendMessageRequest
import sim2.app.talleb_5edma.models.TimeSeparator
import sim2.app.talleb_5edma.models.User
import sim2.app.talleb_5edma.models.getDisplayTime
import sim2.app.talleb_5edma.models.groupWithTimeSeparators
import sim2.app.talleb_5edma.network.ChatRepository
import sim2.app.talleb_5edma.network.UserRepository
import sim2.app.talleb_5edma.network.determineMessageType
import sim2.app.talleb_5edma.network.getFileNameFromUri
import sim2.app.talleb_5edma.util.BASE_URL
import sim2.app.talleb_5edma.util.getToken
import java.io.File
import java.io.FileInputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Red/Pink color theme
val ChatPrimaryColor = Color(0xFFD81B60)
val ChatSurfaceColor = Color(0xFFFFF0F5)
val ChatBubbleSent = Color(0xFFD81B60)
val ChatBubbleReceived = Color(0xFFF5F5F5)

data class PendingMedia(
    val uri: Uri,
    val type: MessageType,
    val fileName: String
)

@Composable
fun ChatScreen(
    navController: androidx.navigation.NavController,
    chatId: String? = null,
    callManager : WebSocketCallManager
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val chatRepository = remember { ChatRepository() }
    val userRepository = remember { UserRepository() }
    val token = remember { getToken(context) }
    val invitationRepository = remember { sim2.app.talleb_5edma.network.InterviewInvitationRepository() } 

    // Invitation Dialog State
    var showInvitationDialog by remember { mutableStateOf(false) }
    var isSendingInvitation by remember { mutableStateOf(false) }

    // State management
    var currentChat by remember { mutableStateOf<GetChatByIdResponse?>(null) }
    var currentUser by remember { mutableStateOf<User?>(null) }
    var messages by remember { mutableStateOf<List<ChatMessage>>(emptyList()) }
    var messageText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    // Media viewer state
    var selectedMedia by remember { mutableStateOf<ChatMessage?>(null) }
    var isMediaViewerVisible by remember { mutableStateOf(false) }

    // Audio recording state
    var isRecording by remember { mutableStateOf(false) }
    var recordingTime by remember { mutableIntStateOf(0) }
    var mediaRecorder: MediaRecorder? by remember { mutableStateOf(null) }
    var audioFile: File? by remember { mutableStateOf(null) }
    var startTime by remember { mutableLongStateOf(0L) }
    var hasAudioPermission by remember { mutableStateOf(false) }

    // Audio playback state (using MediaPlayer like in test screen)
    var mediaPlayer: MediaPlayer? by remember { mutableStateOf(null) }
    var currentlyPlayingAudioId by remember { mutableStateOf<String?>(null) }
    var isPlaying by remember { mutableStateOf(false) }

    // Pending media for sending
    var pendingMedia by remember { mutableStateOf<List<PendingMedia>>(emptyList()) }

    val listState = rememberLazyListState()
    val focusManager = LocalFocusManager.current

    // Function to manually scroll to bottom
    fun scrollToBottom() {
        scope.launch {
            val groupedMessages = messages.groupWithTimeSeparators()
            if (groupedMessages.isNotEmpty()) {
                listState.animateScrollToItem(0)
            }
        }
    }

    // Auto-refresh messages every 30 seconds
    LaunchedEffect(Unit) {
        while (true) {
            delay(30000) // 30 seconds
            if (token.isNotEmpty() && !chatId.isNullOrEmpty()) {
                refreshMessages(token, chatId, chatRepository) { newMessages ->
                    messages = newMessages
                }
            }
        }
    }

    // Auto-scroll to bottom when messages change or screen opens
    LaunchedEffect(messages) {
        if (messages.isNotEmpty() && !isMediaViewerVisible) {
            delay(100)
            // Scroll to index 0 since reverseLayout = true
            listState.animateScrollToItem(0)
        }
    }

    // Auto-scroll when recording state changes (to keep input in view)
    LaunchedEffect(isRecording) {
        if (isRecording) {
            delay(100)
            listState.animateScrollToItem(0)
        }
    }

    // Load current user and chat data
    LaunchedEffect(chatId) {
        if (token.isNotEmpty() && !chatId.isNullOrEmpty()) {
            isLoading = true
            try {
                withContext(Dispatchers.IO) {
                    // Load current user first
                    val userResponse = userRepository.getCurrentUser(token)
                    val currentUserData = userResponse.data ?: User(
                        _id = userResponse._id,
                        nom = userResponse.nom,
                        email = userResponse.email,
                        contact = userResponse.contact,
                        role = userResponse.role,
                        image = userResponse.image,
                        password = userResponse.password,
                        createdAt = userResponse.createdAt,
                        updatedAt = userResponse.updatedAt,
                        modeExamens = userResponse.modeExamens,
                        isArchive = userResponse.isArchive,
                        trustXP = userResponse.trustXP,
                        isOrganization = userResponse.isOrganization,
                        likedOffres = null
                    )

                    // Load existing chat
                    val chatResponse = chatRepository.getChatById(token, chatId)

                    // Load messages
                    val messagesResponse = chatRepository.getMessages(token, chatId)

                    withContext(Dispatchers.Main) {
                        currentUser = currentUserData
                        currentChat = chatResponse
                        messages = messagesResponse.messages
                    }
                }
            } catch (e: Exception) {
                error = "Failed to load chat: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    // Check audio permission on composition
    LaunchedEffect(Unit) {
        hasAudioPermission = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    // Recording timer effect
    LaunchedEffect(isRecording) {
        if (isRecording) {
            startTime = System.currentTimeMillis()
            while (isRecording) {
                delay(100)
                recordingTime = ((System.currentTimeMillis() - startTime) / 1000).toInt()
            }
        }
    }

    val isAtBottom by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val totalItems = layoutInfo.totalItemsCount
            if (totalItems == 0) true
            else {
                // For reverseLayout = true, firstVisibleItemIndex 0 means we're at the bottom
                val firstVisibleIndex = listState.firstVisibleItemIndex
                firstVisibleIndex == 0
            }
        }
    }

    fun stopAudioPlayback() {
        mediaPlayer?.let { player ->
            if (player.isPlaying) {
                player.stop()
            }
            player.release()
            mediaPlayer = null
        }
        isPlaying = false
        currentlyPlayingAudioId = null
    }

    // Audio playback functions - using MediaPlayer like in test screen
    fun playAudio(audioUrl: String, messageId: String) {
        stopAudioPlayback()

        scope.launch {
            try {
                mediaPlayer?.release()
                mediaPlayer = null

                mediaPlayer = MediaPlayer().apply {
                    val correctedUrl = when {
                        audioUrl.startsWith("/") -> audioUrl
                        audioUrl.startsWith("uploads/") -> "$BASE_URL/$audioUrl"
                        audioUrl.contains("localhost") && audioUrl.startsWith("http") -> {
                            audioUrl.replace("http://localhost:3005", BASE_URL)
                        }
                        else -> audioUrl
                    }

                    setDataSource(correctedUrl)
                    setOnPreparedListener {
                        isPlaying = true
                        start()
                        currentlyPlayingAudioId = messageId
                    }
                    setOnCompletionListener {
                        isPlaying = false
                        currentlyPlayingAudioId = null
                        mediaPlayer?.release()
                        mediaPlayer = null
                    }
                    setOnErrorListener { mp, what, extra ->
                        isPlaying = false
                        currentlyPlayingAudioId = null
                        mediaPlayer?.release()
                        mediaPlayer = null
                        true
                    }
                    prepareAsync()
                }
            } catch (e: Exception) {
                isPlaying = false
                currentlyPlayingAudioId = null
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    fun startRecording(context: Context) {
        try {
            stopAudioPlayback()

            mediaRecorder?.release()
            mediaRecorder = null

            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val storageDir = context.cacheDir
            audioFile = File.createTempFile("audio_${timeStamp}_", ".mp3", storageDir)

            mediaRecorder = MediaRecorder(context).apply {
                try {
                    setAudioSource(MediaRecorder.AudioSource.MIC)
                    setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                    setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                    setAudioSamplingRate(44100)
                    setAudioEncodingBitRate(128000)
                    setOutputFile(audioFile?.absolutePath)

                    prepare()
                    start()
                    isRecording = true
                    recordingTime = 0
                    startTime = System.currentTimeMillis()
                } catch (e: Exception) {
                    release()
                    mediaRecorder = null
                    isRecording = false
                }
            }
        } catch (e: Exception) {
            isRecording = false
        }
    }

    // Audio permission launcher
    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasAudioPermission = isGranted
        if (isGranted) {
            startRecording(context)
        }
    }

    // Media picker launcher
    val pickMedia = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia()
    ) { uris ->
        if (uris.isNotEmpty()) {
            val newPendingMedia = uris.map { uri ->
                val fileName = getFileNameFromUri(context, uri) ?: "uploaded_file"
                val messageType = determineMessageType(fileName)
                PendingMedia(uri, messageType, fileName)
            }
            pendingMedia = pendingMedia + newPendingMedia
        }
    }

    fun stopRecordingAndSend() {
        try {
            mediaRecorder?.apply {
                try { stop() } catch (e: Exception) { /* Ignore */ }
                release()
            }
            mediaRecorder = null
            isRecording = false

            val actualDuration = ((System.currentTimeMillis() - startTime) / 1000).toInt()
            recordingTime = actualDuration

            audioFile?.let { file ->
                if (file.exists() && file.length() > 1000) {
                    scope.launch {
                        try {
                            val fileBytes = withContext(Dispatchers.IO) {
                                FileInputStream(file).use { it.readBytes() }
                            }

                            if (fileBytes.isNotEmpty()) {
                                val fileName = "voice_message_${System.currentTimeMillis()}.mp3"
                                val duration = "${actualDuration}s"

                                val uploadResponse = chatRepository.uploadMedia(token, fileBytes, fileName)

                                if (uploadResponse.url.isNotEmpty()) {
                                    val messageRequest = SendMessageRequest(
                                        type = MessageType.AUDIO,
                                        content = "Voice message",
                                        mediaUrl = uploadResponse.url,
                                        fileName = fileName,
                                        fileSize = "%.2f MB".format(fileBytes.size / 1024.0 / 1024.0),
                                        duration = duration
                                    )

                                    val messageResponse = chatRepository.sendMessage(token, chatId!!, messageRequest)
                                    val newMessage = ChatMessage(
                                        id = messageResponse.id,
                                        chat = messageResponse.chat,
                                        sender = messageResponse.sender,
                                        type = messageResponse.type,
                                        content = messageResponse.content,
                                        mediaUrl = messageResponse.mediaUrl,
                                        fileName = messageResponse.fileName,
                                        fileSize = messageResponse.fileSize,
                                        duration = messageResponse.duration,
                                        isRead = messageResponse.isRead,
                                        createdAt = messageResponse.createdAt
                                    )

                                    messages = listOf(newMessage) + messages
                                }
                            }
                        } catch (e: Exception) {
                            error = "Failed to send voice message: ${e.message}"
                        }
                    }
                }
            }
        } catch (e: Exception) {
            error = "Failed to stop recording: ${e.message}"
        }
    }

    // Handle record button click
    fun handleRecordButtonClick() {
        if (isRecording) {
            stopRecordingAndSend()
        } else {
            if (hasAudioPermission) {
                startRecording(context)
            } else {
                audioPermissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
            }
        }
    }

    // Send message function with emoji detection
    fun sendMessage(type: MessageType = MessageType.TEXT, mediaUrl: String? = null, fileName: String? = null, fileSize: String? = null, duration: String? = null) {
        if (currentChat == null || token.isEmpty()) return

        scope.launch {
            try {
                // Determine the actual message type
                val actualType = when {
                    mediaUrl != null -> type
                    messageText.isEmojiOnly() -> {
                        MessageType.EMOJI
                    }
                    else -> {
                        MessageType.TEXT
                    }
                }

                val content = when (actualType) {
                    MessageType.TEXT -> messageText
                    MessageType.EMOJI -> messageText
                    else -> null
                }

                val request = SendMessageRequest(
                    type = actualType,
                    content = content,
                    mediaUrl = mediaUrl,
                    fileName = fileName,
                    fileSize = fileSize,
                    duration = duration
                )

                val response = chatRepository.sendMessage(token, currentChat!!.id, request)

                val newMessage = ChatMessage(
                    id = response.id,
                    chat = response.chat,
                    sender = response.sender,
                    type = response.type,
                    content = response.content,
                    mediaUrl = response.mediaUrl,
                    fileName = response.fileName,
                    fileSize = response.fileSize,
                    duration = response.duration,
                    isRead = response.isRead,
                    createdAt = response.createdAt
                )

                messages = listOf(newMessage) + messages
                messageText = ""
                focusManager.clearFocus()

            } catch (e: Exception) {
                error = "Failed to send message: ${e.message}"
            }
        }
    }

    // Send pending media
    fun sendPendingMedia(media: PendingMedia) {
        scope.launch {
            try {
                val fileBytes = withContext(Dispatchers.IO) {
                    context.contentResolver.openInputStream(media.uri)?.use { it.readBytes() }
                        ?: throw Exception("Could not read file")
                }

                val fileSize = "%.2f MB".format(fileBytes.size / 1024.0 / 1024.0)

                val uploadResponse = chatRepository.uploadMedia(token, fileBytes, media.fileName)

                sendMessage(
                    type = media.type,
                    mediaUrl = uploadResponse.url,
                    fileName = media.fileName,
                    fileSize = fileSize
                )

                pendingMedia = pendingMedia.filter { it.uri != media.uri }

            } catch (e: Exception) {
                error = "Failed to upload media: ${e.message}"
            }
        }
    }

    // Remove pending media
    fun removePendingMedia(media: PendingMedia) {
        pendingMedia = pendingMedia.filter { it.uri != media.uri }
    }

    // Send all pending media
    fun sendAllPendingMedia() {
        pendingMedia.forEach { media ->
            sendPendingMedia(media)
        }
    }

    // Media viewer functions
    fun openMediaViewer(message: ChatMessage) {
        selectedMedia = message
        isMediaViewerVisible = true
    }

    fun closeMediaViewer() {
        isMediaViewerVisible = false
        selectedMedia = null
    }

    // Clean up MediaPlayer when composable leaves composition
    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer?.release()
            mediaRecorder?.release()
        }
    }

    Scaffold(
        topBar = {
            currentChat?.let { chat ->
                ChatTopBar(
                    navController = navController,
                    chat = chat,
                    currentUser = currentUser,
                    onBlockChat = {
                        scope.launch {
                            try {
                                chatRepository.blockChat(token, chat.id, "User blocked chat")
                                currentChat = chatRepository.getChatById(token, chat.id)
                            } catch (e: Exception) {
                                error = "Failed to block chat: ${e.message}"
                            }
                        }
                    },
                    onAcceptCandidate = {
                        scope.launch {
                            try {
                                chatRepository.acceptCandidate(token, chat.id)
                                currentChat = chatRepository.getChatById(token, chat.id)
                            } catch (e: Exception) {
                                error = "Failed to accept candidate: ${e.message}"
                            }
                        }
                    },
                    onSendInterviewInvitation = {
                         showInvitationDialog = true
                    },
                    callManager = callManager // Pass call manager to top bar
                )
            }
        },
        bottomBar = {
            Column {
                // Pending media preview
                if (pendingMedia.isNotEmpty()) {
                    PendingMediaRow(
                        pendingMedia = pendingMedia,
                        onRemoveMedia = { removePendingMedia(it) },
                        onSendAll = { sendAllPendingMedia() }
                    )
                }

                ChatBottomBar(
                    messageText = messageText,
                    onMessageTextChange = { messageText = it },
                    onSendMessage = { sendMessage() },
                    onEmojiAdded = { emoji ->
                        messageText += emoji // Append emoji instead of replacing
                    },
                    onAttachMedia = {
                        pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo))
                    },
                    onRecordAudio = { handleRecordButtonClick() },
                    isRecording = isRecording,
                    recordingTime = recordingTime
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(ChatSurfaceColor)
        ) {
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = ChatPrimaryColor)
                }
            } else if (error != null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(error!!, color = MaterialTheme.colorScheme.error)
                }
            } else if (currentChat == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No chat selected", color = Color.Gray)
                }
            } else {
                ChatMessagesList(
                    messages = messages,
                    currentUserId = currentUser?._id ?: "",
                    listState = listState,
                    currentlyPlayingAudioId = currentlyPlayingAudioId,
                    isPlaying = isPlaying,
                    onPlayAudio = { audioUrl, messageId -> playAudio(audioUrl, messageId) },
                    onStopAudio = { stopAudioPlayback() },
                    onMediaClick = { message -> openMediaViewer(message) }
                )
            }

            // Scroll to bottom button when not at bottom
            if (!isAtBottom && messages.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                ) {
                    FloatingActionButton(
                        onClick = { scrollToBottom() },
                        containerColor = ChatPrimaryColor,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            Icons.Default.ArrowDownward,
                            contentDescription = "Scroll to bottom",
                            tint = Color.White
                        )
                    }
                }
            }

            // Media Viewer Dialog
            if (isMediaViewerVisible && selectedMedia != null) {
                MediaViewerDialog(
                    message = selectedMedia!!,
                    onClose = { closeMediaViewer() }
                )
            }
            // Interview Invitation Confirmation Dialog
            if (showInvitationDialog) {
                AlertDialog(
                    onDismissRequest = { showInvitationDialog = false },
                    title = { Text(text = "Send Interview Invitation?") },
                    text = { 
                        Text("Do you want to invite this candidate to an AI Mock Interview? They will receive a notification to join.") 
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                isSendingInvitation = true
                                scope.launch {
                                    try {
                                        if (currentChat != null && currentUser != null) {
                                            val candidateId = currentChat?.candidate?.id ?: ""
                                            val offerId = currentChat?.offer?.id
                                            
                                            val response = withContext(Dispatchers.IO) {
                                                invitationRepository.sendInvitation(
                                                    chatId = currentChat!!.id,
                                                    fromUserId = currentUser!!._id ?: "",
                                                    toUserId = candidateId,
                                                    fromUserName = currentUser!!.nom ?: "User", // Fallback for nullable
                                                    offerId = offerId
                                                )
                                            }
                                            
                                            if (response.success) {

                                            } else {
                                                error = "Failed to send invitation: ${response.error}"
                                            }
                                        }
                                    } catch (e: Exception) {
                                        error = "Error: ${e.message}"
                                    } finally {
                                        isSendingInvitation = false
                                        showInvitationDialog = false
                                    }
                                }
                            },
                            enabled = !isSendingInvitation
                        ) {
                            if (isSendingInvitation) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                            } else {
                                Text("Send Invitation")
                            }
                        }
                    },
                    dismissButton = {
                        Button(onClick = { showInvitationDialog = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
    }
}

// Simple emoji detection
private fun String.isEmojiOnly(): Boolean {
    if (this.isBlank()) return false

    val trimmed = this.trim()
    if (trimmed.isEmpty()) return false

    // Common emoji code point ranges
    val emojiRanges = listOf(
        "😀", "😃", "😄", "😁", "😆", "😅", "😂", "🤣", "😊", "😇", "🙂", "🙃", "😉", "😌", "😍", "🥰", "😘", "😗", "😙", "😚", "😋", "😛", "😝", "😜", "🤪", "🤨", "🧐", "🤓", "😎", "🤩", "🥳", "😏", "😒", "😞", "😔", "😟", "😕", "🙁", "☹️", "😣", "😖", "😫", "😩", "🥺", "😢", "😭", "😤", "😠", "😡", "🤬", "🤯", "😳", "🥵", "🥶", "😱", "😨", "😰", "😥", "😓", "🤗", "🤔", "🤭", "🤫", "🤥", "😶", "😐", "😑", "😬", "🙄", "😯", "😦", "😧", "😮", "😲", "🥱", "😴", "🤤", "😪", "😵", "🤐", "🥴", "🤢", "🤮", "🤧", "😷", "🤒", "🤕",
        "❤️", "🧡", "💛", "💚", "💙", "💜", "🖤", "🤍", "🤎", "💔", "❣️", "💕", "💞", "💓", "💗", "💖", "💘", "💝",
        "👍", "👎", "👊", "✊", "🤛", "🤜", "🤞", "✌️", "🤟", "🤘", "👌", "👈", "👉", "👆", "👇", "☝️", "✋", "🤚", "🖐️", "🖖", "👋", "🤙", "💪", "🦾", "🖕", "✍️", "🙏", "🦶", "🦿", "🦵",
        "🎉", "🎊", "🎁", "🎈", "🎂", "🍰", "🧁", "🥳", "🍾", "🍻", "🥂", "🎇", "🎆", "✨", "🎄", "🎅", "🤶", "👼", "💫", "⭐", "🌟", "💥", "🔥", "💯"
    )

    // Check if all characters are emojis or whitespace
    return trimmed.all { char ->
        char.isWhitespace() || emojiRanges.any { range -> char.toString() in range }
    } && trimmed.count { !it.isWhitespace() } in 1..10
}

// Function to refresh messages
private suspend fun refreshMessages(
    token: String,
    chatId: String,
    chatRepository: ChatRepository,
    onMessagesLoaded: (List<ChatMessage>) -> Unit
) {
    try {
        val messagesResponse = chatRepository.getMessages(token, chatId)
        onMessagesLoaded(messagesResponse.messages)
    } catch (e: Exception) {
        // Silently fail for auto-refresh
    }
}

@Composable
fun ChatTopBar(
    navController: androidx.navigation.NavController,
    chat: GetChatByIdResponse,
    currentUser: User?,
    onBlockChat: () -> Unit,
    onAcceptCandidate: () -> Unit,
    onSendInterviewInvitation: () -> Unit = {}, // NEW CALLBACK
    callManager: WebSocketCallManager? = null
) {
    val isCurrentUserCandidate = currentUser?._id == chat.candidate?.id
    val otherUser = if (isCurrentUserCandidate) chat.entreprise else chat.candidate
    callManager?.toUserName = otherUser?.nom ?: ""
    callManager?.toUserImage = otherUser?.image ?: ""

    Surface(
        color = ChatPrimaryColor,
        shadowElevation = 4.dp
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(70.dp)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                Spacer(Modifier.width(12.dp))

                // User Avatar
                Box(
                    modifier = Modifier
                        .size(45.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (!otherUser?.image.isNullOrEmpty()) {
                        val imageUrl = "$BASE_URL/${otherUser.image}"
                        AsyncImage(
                            model = imageUrl,
                            contentDescription = "Avatar",
                            modifier = Modifier
                                .size(45.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            Icons.Default.AccountCircle,
                            contentDescription = "Avatar",
                            tint = Color.White,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }

                Spacer(Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = otherUser?.nom ?: "Unknown User",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp
                    )
                    Text(
                        text = if (chat.isAccepted == true) "✓ Candidate Accepted" else "Active",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 12.sp
                    )
                }

                // Action buttons - show call icons when candidate is accepted
                if (chat.isAccepted == true) {
                    Row {
                        IconButton(
                            onClick = {
                                // Start video call through call manager
                                callManager?.makeCall(otherUser?.id ?: "", true, chat.id)
                                // Don't navigate directly - let the state change handle navigation
                            },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Videocam,
                                contentDescription = "Video Call",
                                tint = Color.White
                            )
                        }
                        IconButton(
                            onClick = {
                                // Start audio call through call manager
                                callManager?.makeCall(otherUser?.id ?: "", false, chat.id)
                                // Don't navigate directly - let the state change handle navigation
                            },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Call,
                                contentDescription = "Audio Call",
                                tint = Color.White
                            )
                        }
                        IconButton(
                            onClick = {
                                // Different behavior based on user type
                                if (isCurrentUserCandidate) {
                                    // Student: Navigate directly to AI coaching mode
                                    navController.navigate("${sim2.app.talleb_5edma.Routes.AiInterviewTraining}/${chat.id}/coaching")
                                } else {
                                    // Enterprise: Send interview invitation to student
                                    onSendInterviewInvitation()
                                }
                            },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AllInclusive,
                                contentDescription = if (isCurrentUserCandidate) "AI Coach" else "Send Interview",
                                tint = Color.White
                            )
                        }
                    }
                }
                // Original action buttons for entreprise user
                if (!isCurrentUserCandidate) {
                    Row {
                        if (chat.isAccepted != true) {
                            IconButton(
                                onClick = onAcceptCandidate,
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Accept Candidate",
                                    tint = Color.White
                                )
                            }
                        }

                        IconButton(
                            onClick = onBlockChat,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Block,
                                contentDescription = "Block Chat",
                                tint = Color.White
                            )
                        }
                    }
                }
            }

            // Chat status bar
            if (chat.isBlocked == true) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.error)
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Chat is blocked",
                        color = Color.White,
                        fontSize = 12.sp
                    )
                }
            } else if (chat.isAccepted == true) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF4CAF50))
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Candidate accepted - Calls enabled",
                        color = Color.White,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
fun MediaViewerDialog(
    message: ChatMessage,
    onClose: () -> Unit
) {
    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        when (message.type) {
            MessageType.IMAGE -> {
                ImageViewer(
                    message = message,
                    onClose = onClose
                )
            }
            MessageType.VIDEO -> {
                MediaPlayerVideoViewer(
                    message = message,
                    onClose = onClose
                )
            }
            else -> {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .wrapContentHeight(),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Media Preview",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Preview not available for this media type",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onClose,
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("Close")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ImageViewer(
    message: ChatMessage,
    onClose: () -> Unit
) {
    val imageUrl = if (!message.mediaUrl.isNullOrEmpty()) {
        if (message.mediaUrl.startsWith("http")) {
            message.mediaUrl.replace("localhost:3005", "10.0.2.2:3005")
        } else {
            "$BASE_URL/${message.mediaUrl}"
        }
    } else ""

    Surface(
        modifier = Modifier
            .fillMaxWidth(0.95f)
            .fillMaxHeight(0.95f)
            .background(Color.Black),
        color = Color.Black,
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (imageUrl.isNotEmpty()) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = "Full screen image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Image not available", color = Color.White)
                }
            }

            // Close button
            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
                    .size(40.dp)
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Color.White
                )
            }

            // Image info
            if (!message.fileName.isNullOrEmpty()) {
                Text(
                    text = message.fileName,
                    color = Color.White,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                        .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .padding(8.dp)
                )
            }
        }
    }
}

@Composable
fun MediaPlayerVideoViewer(
    message: ChatMessage,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    var mediaPlayer: MediaPlayer? by remember { mutableStateOf(null) }
    var isPlaying by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    var showControls by remember { mutableStateOf(true) }
    var videoDuration by remember { mutableIntStateOf(0) }
    var currentPosition by remember { mutableIntStateOf(0) }
    var hasError by remember { mutableStateOf(false) }

    // Build the correct video URL
    val videoUrl = remember(message.mediaUrl) {
        if (!message.mediaUrl.isNullOrEmpty()) {
            when {
                message.mediaUrl.startsWith("http") -> {
                    message.mediaUrl.replace("localhost:3005", "10.0.2.2:3005")
                }
                message.mediaUrl.startsWith("/") -> "$BASE_URL${message.mediaUrl}"
                else -> "$BASE_URL/${message.mediaUrl}"
            }
        } else ""
    }

    // Auto-hide controls
    LaunchedEffect(isPlaying, showControls) {
        if (isPlaying && showControls) {
            delay(3000)
            showControls = false
        }
    }

    // Update current position periodically
    LaunchedEffect(isPlaying) {
        while (true) {
            delay(1000)
            if (isPlaying) {
                mediaPlayer?.let { player ->
                    currentPosition = player.currentPosition
                }
            }
        }
    }

    // Clean up MediaPlayer
    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer?.let { player ->
                if (player.isPlaying) {
                    player.stop()
                }
                player.release()
            }
            mediaPlayer = null
        }
    }

    Dialog(
        onDismissRequest = {
            mediaPlayer?.let { player ->
                if (player.isPlaying) {
                    player.pause()
                }
            }
            onClose()
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.95f),
            color = Color.Black,
            shape = RoundedCornerShape(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable {
                        showControls = !showControls
                    }
            ) {
                if (videoUrl.isNotEmpty() && !hasError) {
                    // SurfaceView for video rendering
                    AndroidView(
                        factory = { ctx ->
                            SurfaceView(ctx).apply {
                                holder.addCallback(object : SurfaceHolder.Callback {
                                    override fun surfaceCreated(holder: SurfaceHolder) {
                                        try {
                                            MediaPlayer().apply {
                                                mediaPlayer = this

                                                setOnPreparedListener { mp ->
                                                    isLoading = false
                                                    videoDuration = duration
                                                    setDisplay(holder)
                                                    start()
                                                    isPlaying = true
                                                    showControls = true
                                                }

                                                setOnErrorListener { mp, what, extra ->
                                                    isLoading = false
                                                    hasError = true
                                                    android.util.Log.e("MediaPlayerVideo", "Error: what=$what, extra=$extra")
                                                    true
                                                }

                                                setOnCompletionListener {
                                                    isPlaying = false
                                                    currentPosition = 0
                                                    seekTo(0)
                                                    showControls = true
                                                }

                                                setOnInfoListener { mp, what, extra ->
                                                    when (what) {
                                                        MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START -> {
                                                            isLoading = false
                                                        }
                                                        MediaPlayer.MEDIA_INFO_BUFFERING_START -> {
                                                            isLoading = true
                                                        }
                                                        MediaPlayer.MEDIA_INFO_BUFFERING_END -> {
                                                            isLoading = false
                                                        }
                                                    }
                                                    true
                                                }

                                                setDataSource(videoUrl)
                                                prepareAsync()
                                            }
                                        } catch (e: Exception) {
                                            hasError = true
                                            android.util.Log.e("MediaPlayerVideo", "Setup error: ${e.message}")
                                        }
                                    }

                                    override fun surfaceChanged(
                                        holder: SurfaceHolder,
                                        format: Int,
                                        width: Int,
                                        height: Int
                                    ) {
                                        // Handle surface changes
                                    }

                                    override fun surfaceDestroyed(holder: SurfaceHolder) {
                                        mediaPlayer?.setDisplay(null)
                                    }
                                })
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    // Error state or no video URL
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            if (hasError) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Error",
                                    tint = Color.White,
                                    modifier = Modifier.size(48.dp)
                                )
                                Text(
                                    text = "Failed to load video",
                                    color = Color.White,
                                    fontSize = 16.sp
                                )
                            } else {
                                Text(
                                    text = "Video not available",
                                    color = Color.White,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }
                }

                // Loading indicator
                if (isLoading && !hasError) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CircularProgressIndicator(color = Color.White)
                            Text(
                                text = "Loading video...",
                                color = Color.White,
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                // Top bar with close button
                if (showControls) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(16.dp)
                    ) {
                        Surface(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .clickable {
                                    mediaPlayer?.let { player ->
                                        if (player.isPlaying) {
                                            player.pause()
                                        }
                                    }
                                    onClose()
                                },
                            color = Color.Black.copy(alpha = 0.7f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Close",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }

                // Video info
                if (!message.fileName.isNullOrEmpty() && showControls) {
                    Text(
                        text = message.fileName,
                        color = Color.White,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 16.dp, start = 60.dp, end = 60.dp)
                            .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }

                // CENTERED Play/Pause button - always visible when not playing and controls are shown
                if ((showControls && !isPlaying) || (!isPlaying && !isLoading && !hasError)) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable {
                                if (!isLoading && !hasError) {
                                    mediaPlayer?.let { player ->
                                        if (!player.isPlaying) {
                                            player.start()
                                            isPlaying = true
                                            showControls = true
                                        }
                                    }
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape),
                            color = Color.Black.copy(alpha = 0.6f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.PlayArrow,
                                    contentDescription = "Play",
                                    tint = Color.White,
                                    modifier = Modifier.size(40.dp)
                                )
                            }
                        }
                    }
                }

                // Bottom controls
                if (showControls && !isLoading && !hasError) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .height(100.dp)
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 0.8f)
                                    )
                                )
                            )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 20.dp, vertical = 16.dp)
                        ) {
                            // Progress bar
                            if (videoDuration > 0) {
                                LinearProgressIndicator(
                                    progress = {
                                        if (videoDuration > 0) {
                                            currentPosition.toFloat() / videoDuration.toFloat()
                                        } else 0f
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(4.dp),
                                    color = ChatPrimaryColor,
                                    trackColor = Color.White.copy(alpha = 0.3f)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                // Current time
                                Text(
                                    text = formatTime(currentPosition),
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )

                                // Play/Pause button (duplicate in bottom for convenience)
                                IconButton(
                                    onClick = {
                                        mediaPlayer?.let { player ->
                                            if (isPlaying) {
                                                player.pause()
                                                isPlaying = false
                                            } else {
                                                player.start()
                                                isPlaying = true
                                            }
                                            showControls = true
                                        }
                                    },
                                    modifier = Modifier
                                        .size(50.dp)
                                        .background(
                                            Color.White.copy(alpha = 0.2f),
                                            CircleShape
                                        )
                                ) {
                                    Icon(
                                        if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                        contentDescription = if (isPlaying) "Pause" else "Play",
                                        tint = Color.White,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }

                                // Total duration
                                Text(
                                    text = formatTime(videoDuration),
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                // Error retry button
                if (hasError) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(bottom = 80.dp)
                    ) {
                        Button(
                            onClick = {
                                hasError = false
                                isLoading = true
                                // Retry loading
                                mediaPlayer?.release()
                                mediaPlayer = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ChatPrimaryColor)
                        ) {
                            Text("Retry")
                        }
                    }
                }
            }
        }
    }
}

// Helper function to format time (make sure this exists)
private fun formatTime(milliseconds: Int): String {
    val seconds = milliseconds / 1000
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return String.format(Locale.getDefault(), "%02d:%02d", minutes, remainingSeconds)
}

@Composable
fun PendingMediaRow(
    pendingMedia: List<PendingMedia>,
    onRemoveMedia: (PendingMedia) -> Unit,
    onSendAll: () -> Unit
) {
    Surface(
        color = Color.White,
        shadowElevation = 4.dp
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Pending media: ${pendingMedia.size}",
                    modifier = Modifier.weight(1f),
                    fontWeight = FontWeight.Medium
                )
                Button(
                    onClick = onSendAll,
                    colors = ButtonDefaults.buttonColors(containerColor = ChatPrimaryColor)
                ) {
                    Text("Send All")
                }
            }
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(pendingMedia) { media ->
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(8.dp))
                    ) {
                        // Use actual URI for thumbnails
                        AsyncImage(
                            model = media.uri,
                            contentDescription = "Pending media",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )

                        // Remove button
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Color.Red.copy(alpha = 0.8f))
                                .clickable { onRemoveMedia(media) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("×", color = Color.White, fontWeight = FontWeight.Bold)
                        }

                        // Media type indicator
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(4.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color.Black.copy(alpha = 0.6f))
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text(
                                when (media.type) {
                                    MessageType.IMAGE -> "IMG"
                                    MessageType.VIDEO -> "VID"
                                    else -> "FILE"
                                },
                                color = Color.White,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChatMessagesList(
    messages: List<ChatMessage>,
    currentUserId: String,
    listState: androidx.compose.foundation.lazy.LazyListState,
    currentlyPlayingAudioId: String?,
    isPlaying: Boolean,
    onPlayAudio: (String, String) -> Unit,
    onStopAudio: () -> Unit,
    onMediaClick: (ChatMessage) -> Unit,
    modifier: Modifier = Modifier
) {
    val groupedMessages = remember(messages) {
        messages.groupWithTimeSeparators().reversed()
    }

    // CORRECTED Auto-scroll when new messages arrive for reverseLayout
    LaunchedEffect(groupedMessages.size) {
        if (groupedMessages.isNotEmpty()) {
            // For reverseLayout = true, check if we're at the bottom (index 0)
            val firstVisibleIndex = listState.firstVisibleItemIndex
            val shouldAutoScroll = firstVisibleIndex == 0

            if (shouldAutoScroll) {
                delay(100)
                listState.animateScrollToItem(0) // Scroll to bottom (newest message)
            }
        }
    }

    Box(modifier = modifier) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(ChatSurfaceColor),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            state = listState,
            reverseLayout = true // KEEP THIS TRUE as requested
        ) {
            itemsIndexed(groupedMessages) { index, item ->
                when (item) {
                    is ChatListItem.MessageItem -> {
                        val message = item.message
                        val isSent = message.sender?.id == currentUserId
                        val isAudioPlaying = currentlyPlayingAudioId == message.id && isPlaying

                        when (message.type) {
                            MessageType.TEXT -> TextMessageItem(message, isSent)
                            MessageType.IMAGE -> ImageMessageItem(
                                message = message,
                                isSent = isSent,
                                onMediaClick = onMediaClick
                            )
                            MessageType.VIDEO -> VideoMessageItem(
                                message = message,
                                isSent = isSent,
                                onMediaClick = onMediaClick
                            )
                            MessageType.AUDIO -> AudioMessageItem(
                                message,
                                isSent,
                                isPlaying = isAudioPlaying,
                                onPlayAudio = { onPlayAudio(message.mediaUrl ?: "", message.id) },
                                onStopAudio = onStopAudio
                            )
                            MessageType.EMOJI -> EmojiMessageItem(message, isSent)
                            MessageType.INTERVIEW_RESULT -> {
                                // Only show interview results to enterprise (receiver, not sender)
                                if (!isSent) {
                                    InterviewResultMessageItem(message, isSent)
                                }
                            }
                        }
                    }
                    is ChatListItem.TimeSeparatorItem -> {
                        TimeSeparatorItem(item.separator)
                    }
                }
            }
        }
    }
}

@Composable
fun TextMessageItem(message: ChatMessage, isSent: Boolean) {
    val horizontalArrangement = if (isSent) Arrangement.End else Arrangement.Start
    val bubbleColor = if (isSent) ChatBubbleSent else ChatBubbleReceived
    val textColor = if (isSent) Color.White else Color.Black

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = horizontalArrangement
    ) {
        Column(
            horizontalAlignment = if (isSent) Alignment.End else Alignment.Start,
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(
                    topStart = 18.dp,
                    topEnd = 18.dp,
                    bottomStart = if (isSent) 18.dp else 4.dp,
                    bottomEnd = if (isSent) 4.dp else 18.dp
                ),
                color = bubbleColor,
                shadowElevation = 1.dp
            ) {
                Text(
                    text = message.content ?: "",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    fontSize = 16.sp,
                    color = textColor,
                    maxLines = 10
                )
            }

            Text(
                text = message.getDisplayTime(),
                fontSize = 10.sp,
                color = Color.Gray,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
            )
        }
    }
}

@Composable
fun ImageMessageItem(message: ChatMessage, isSent: Boolean, onMediaClick: (ChatMessage) -> Unit) {
    val horizontalArrangement = if (isSent) Arrangement.End else Arrangement.Start

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = horizontalArrangement
    ) {
        Column(
            horizontalAlignment = if (isSent) Alignment.End else Alignment.Start,
            modifier = Modifier.padding(horizontal = 8.dp)
        ){
            if (!message.mediaUrl.isNullOrEmpty()) {
                val imageUrl = if (message.mediaUrl.startsWith("http")) {
                    message.mediaUrl.replace("localhost:3005", "10.0.2.2:3005")
                } else {
                    "$BASE_URL/${message.mediaUrl}"
                }

                AsyncImage(
                    model = imageUrl,
                    contentDescription = "Image message",
                    modifier = Modifier
                        .width(250.dp)
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onMediaClick(message) },
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .width(200.dp)
                        .height(150.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.LightGray)
                        .clickable { onMediaClick(message) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Image, "Image", tint = Color.White)
                }
            }

            Text(
                text = message.getDisplayTime(),
                fontSize = 10.sp,
                color = Color.Gray,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
            )
        }
    }
}

@Composable
fun VideoMessageItem(message: ChatMessage, isSent: Boolean, onMediaClick: (ChatMessage) -> Unit) {
    val horizontalArrangement = if (isSent) Arrangement.End else Arrangement.Start

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = horizontalArrangement
    ) {
        Column(
            horizontalAlignment = if (isSent) Alignment.End else Alignment.Start
        ) {
            Box(
                modifier = Modifier
                    .width(250.dp)
                    .height(150.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black)
                    .clickable { onMediaClick(message) },
                contentAlignment = Alignment.Center
            ) {
                // Show video thumbnail if available, otherwise placeholder
                if (!message.mediaUrl.isNullOrEmpty()) {
                    // You can use a proper video thumbnail library here
                    // For now, showing play icon over black background
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = "Play Video",
                        tint = Color.White,
                        modifier = Modifier.size(40.dp)
                    )
                } else {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = "Play Video",
                        tint = Color.White,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }

            Text(
                text = message.getDisplayTime(),
                fontSize = 10.sp,
                color = Color.Gray,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
            )
        }
    }
}

@Composable
fun AudioMessageItem(
    message: ChatMessage,
    isSent: Boolean,
    isPlaying: Boolean,
    onPlayAudio: () -> Unit,
    onStopAudio: () -> Unit
) {
    val horizontalArrangement = if (isSent) Arrangement.End else Arrangement.Start

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = horizontalArrangement
    ) {
        Column(
            horizontalAlignment = if (isSent) Alignment.End else Alignment.Start
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (isSent) ChatBubbleSent else ChatBubbleReceived)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                if (!isSent) {
                    IconButton(
                        onClick = {
                            if (isPlaying) onStopAudio() else onPlayAudio()
                        },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause Audio" else "Play Audio",
                            tint = if (isSent) Color.White else Color.Black,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                }

                Text(
                    text = message.duration ?: "0:00",
                    color = if (isSent) Color.White else Color.Black,
                    fontSize = 14.sp
                )

                Spacer(Modifier.width(8.dp))
                Row {
                    repeat(5) { index ->
                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .height((10 + index * 4).dp)
                                .background(if (isSent) Color.White else Color.Black)
                                .padding(horizontal = 1.dp)
                        )
                    }
                }

                if (isSent) {
                    Spacer(Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (isPlaying) onStopAudio() else onPlayAudio()
                        },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause Audio" else "Play Audio",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Text(
                text = message.getDisplayTime(),
                fontSize = 10.sp,
                color = Color.Gray,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
            )
        }
    }
}

@Composable
fun EmojiMessageItem(message: ChatMessage, isSent: Boolean) {
    val horizontalArrangement = if (isSent) Arrangement.End else Arrangement.Start

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = horizontalArrangement
    ) {
        Column(
            horizontalAlignment = if (isSent) Alignment.End else Alignment.Start,
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            // Larger emoji display without background for emoji-only messages
            Text(
                text = message.content ?: "",
                fontSize = 48.sp, // Much larger font for emoji
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            Text(
                text = message.getDisplayTime(),
                fontSize = 10.sp,
                color = Color.Gray,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
            )
        }
    }
}

@Composable
fun TimeSeparatorItem(separator: TimeSeparator) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = ChatPrimaryColor.copy(alpha = 0.1f)
        ) {
            Text(
                text = separator.text,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = ChatPrimaryColor,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
fun ChatBottomBar(
    messageText: String,
    onMessageTextChange: (String) -> Unit,
    onSendMessage: () -> Unit,
    onEmojiAdded: (String) -> Unit,
    onAttachMedia: () -> Unit,
    onRecordAudio: () -> Unit,
    isRecording: Boolean = false,
    recordingTime: Int = 0
) {
    var showEmojiPicker by remember { mutableStateOf(false) }
    var selectedEmojiCategory by remember { mutableStateOf("Smileys") }
    val focusManager = LocalFocusManager.current

    Surface(
        color = Color.White,
        shadowElevation = 8.dp,
        border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFFE0E0E0))
    ) {
        Column {
            if (showEmojiPicker) {
                EnhancedEmojiPicker(
                    selectedCategory = selectedEmojiCategory,
                    onCategorySelected = { selectedEmojiCategory = it },
                    onEmojiSelected = { emoji ->
                        onEmojiAdded(emoji) // Append emoji instead of replacing
                    }
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(70.dp)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Emoji button
                IconButton(
                    onClick = {
                        showEmojiPicker = !showEmojiPicker
                        if (showEmojiPicker) {
                            focusManager.clearFocus()
                        }
                    },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        Icons.Default.EmojiEmotions,
                        contentDescription = "Emoji",
                        tint = ChatPrimaryColor
                    )
                }

                // Message input
                TextField(
                    value = messageText,
                    onValueChange = onMessageTextChange,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 40.dp, max = 120.dp),
                    placeholder = { Text("Type a message...") },
                    shape = RoundedCornerShape(20.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFF5F5F5),
                        unfocusedContainerColor = Color(0xFFF5F5F5),
                        disabledContainerColor = Color(0xFFF5F5F5),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black
                    ),
                    singleLine = false,
                    maxLines = 5
                )

                // Send/Record button
                if (messageText.isNotBlank()) {
                    IconButton(
                        onClick = {
                            onSendMessage()
                            focusManager.clearFocus()
                        },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send Message",
                            tint = ChatPrimaryColor
                        )
                    }
                } else {
                    if (isRecording) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${recordingTime}s",
                                color = ChatPrimaryColor,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.width(8.dp))
                            IconButton(
                                onClick = {
                                    onRecordAudio()
                                    focusManager.clearFocus()
                                },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    Icons.Default.Stop,
                                    contentDescription = "Stop Recording",
                                    tint = Color.Red
                                )
                            }
                        }
                    } else {
                        IconButton(
                            onClick = {
                                onRecordAudio()
                                focusManager.clearFocus()
                            },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                Icons.Default.Mic,
                                contentDescription = "Record Audio",
                                tint = ChatPrimaryColor
                            )
                        }
                    }
                }

                // Attachment button
                IconButton(
                    onClick = {
                        onAttachMedia()
                        focusManager.clearFocus()
                    },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        Icons.Default.AttachFile,
                        contentDescription = "Attach File",
                        tint = ChatPrimaryColor
                    )
                }
            }
        }
    }
}

@Composable
fun EnhancedEmojiPicker(
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    onEmojiSelected: (String) -> Unit
) {
    val emojiCategories = mapOf(
        "Smileys" to listOf(
            "😀", "😃", "😄", "😁", "😆", "😅", "😂", "🤣", "😊", "😇", "🙂", "🙃", "😉", "😌", "😍", "🥰", "😘", "😗", "😙", "😚", "😋", "😛", "😝", "😜", "🤪", "🤨", "🧐", "🤓", "😎", "🤩", "🥳", "😏", "😒", "😞", "😔", "😟", "😕", "🙁", "☹️", "😣", "😖", "😫", "😩", "🥺", "😢", "😭", "😤", "😠", "😡", "🤬", "🤯", "😳", "🥵", "🥶", "😱", "😨", "😰", "😥", "😓", "🤗", "🤔", "🤭", "🤫", "🤥", "😶", "😐", "😑", "😬", "🙄", "😯", "😦", "😧", "😮", "😲", "🥱", "😴", "🤤", "😪", "😵", "🤐", "🥴", "🤢", "🤮", "🤧", "😷", "🤒", "🤕"
        ),
        "Hearts" to listOf("❤️", "🧡", "💛", "💚", "💙", "💜", "🖤", "🤍", "🤎", "💔", "❣️", "💕", "💞", "💓", "💗", "💖", "💘", "💝"),
        "Hands" to listOf("👍", "👎", "👊", "✊", "🤛", "🤜", "🤞", "✌️", "🤟", "🤘", "👌", "👈", "👉", "👆", "👇", "☝️", "✋", "🤚", "🖐️", "🖖", "👋", "🤙", "💪", "🦾", "🖕", "✍️", "🙏", "🦶", "🦿", "🦵"),
        "Celebration" to listOf("🎉", "🎊", "🎁", "🎈", "🎂", "🍰", "🧁", "🥳", "🍾", "🍻", "🥂", "🎇", "🎆", "✨", "🎄", "🎅", "🤶", "👼", "💫", "⭐", "🌟", "💥", "🔥", "💯")
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp) // Reduced from 250.dp to 150.dp
            .background(Color.White),
    ) {
        // Category tabs
        TabRow(
            selectedTabIndex = emojiCategories.keys.indexOf(selectedCategory),
            containerColor = Color.White,
            contentColor = ChatPrimaryColor,
            modifier = Modifier.fillMaxWidth()
        ) {
            emojiCategories.keys.forEach { category ->
                Tab(
                    selected = category == selectedCategory,
                    onClick = { onCategorySelected(category) },
                    text = {
                        Text(
                            text = category,
                            fontSize = 12.sp,
                            maxLines = 1
                        )
                    }
                )
            }
        }

        // Emoji grid for selected category
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(8.dp)
        ) {
            val selectedEmojis = emojiCategories[selectedCategory] ?: emptyList()
            items(selectedEmojis.chunked(8)) { rowEmojis ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    rowEmojis.forEach { emoji ->
                        Text(
                            text = emoji,
                            fontSize = 24.sp,
                            modifier = Modifier
                                .clickable { onEmojiSelected(emoji) }
                                .padding(8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun InterviewResultMessageItem(message: ChatMessage, isSent: Boolean) {
    val analysis = message.interviewAnalysis ?: return
    var showFullDialog by remember { mutableStateOf(false) }
    
    // Distinct color for analysis results
    val cardColor = Color(0xFFF5F7FA) 
    val borderColor = ChatPrimaryColor.copy(alpha = 0.3f)

    // Summary Card (Click to expand)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = if (isSent) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = cardColor,
            shadowElevation = 2.dp,
            border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
            modifier = Modifier
                .widthIn(max = 300.dp)
                .clickable { showFullDialog = true }
        ) {
             Column(modifier = Modifier.padding(16.dp)) {
                 Row(
                     verticalAlignment = Alignment.CenterVertically,
                     horizontalArrangement = Arrangement.SpaceBetween,
                     modifier = Modifier.fillMaxWidth()
                 ) {
                     Text(
                         text = "📊 AI Analysis", 
                         fontWeight = FontWeight.Bold, 
                         color = ChatPrimaryColor,
                         fontSize = 16.sp
                     )
                     Text(
                         text = "${analysis.overallScore}%", 
                         fontWeight = FontWeight.Black, 
                         fontSize = 18.sp,
                         color = if (analysis.overallScore >= 70) Color(0xFF2E7D32) else Color(0xFFC62828)
                     )
                 }
                 
                 Spacer(modifier = Modifier.height(8.dp))
                 
                 Text(
                    text = "Candidate: ${analysis.candidateName}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                 )
                 
                 Spacer(modifier = Modifier.height(4.dp))

                 // Recommendation
                 val recColor = when(analysis.recommendation) {
                     "STRONG_HIRE" -> Color(0xFF00C853)
                     "HIRE" -> Color(0xFF2E7D32)
                     "MAYBE" -> Color(0xFFF9A825)
                     "NO_HIRE" -> Color(0xFFC62828)
                     else -> Color.Gray
                 }
                 
                 Surface(
                     color = recColor.copy(alpha = 0.1f),
                     shape = RoundedCornerShape(4.dp),
                     modifier = Modifier.fillMaxWidth()
                 ) {
                     Text(
                         text = "Rec: ${analysis.recommendation.replace("_", " ")}", 
                         color = recColor, 
                         fontWeight = FontWeight.Bold,
                         modifier = Modifier.padding(8.dp),
                         textAlign = androidx.compose.ui.text.style.TextAlign.Center
                     )
                 }
                 
                 Spacer(modifier = Modifier.height(8.dp))
                 
                 if (analysis.strengths.isNotEmpty()) {
                     Text("✅ Key Strengths:", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                     analysis.strengths.take(3).forEach { 
                         Text("• $it", fontSize = 12.sp, color = Color.DarkGray, maxLines = 1, overflow = TextOverflow.Ellipsis)
                     }
                 }
                 
                 Spacer(modifier = Modifier.height(8.dp))
                 
                 // Click to view full details hint
                 Text(
                     text = "👆 Tap for full details",
                     fontSize = 11.sp,
                     color = ChatPrimaryColor,
                     fontWeight = FontWeight.Medium,
                     modifier = Modifier.align(Alignment.CenterHorizontally)
                 )
                 
                 // Timestamp at bottom
                 Spacer(modifier = Modifier.height(4.dp))
                 Text(
                     text = message.getDisplayTime(),
                     fontSize = 10.sp,
                     color = Color.Gray,
                     modifier = Modifier.align(Alignment.End)
                 )
             }
        }
    }
    
    // Full Details Dialog
    if (showFullDialog) {
        androidx.compose.ui.window.Dialog(
            onDismissRequest = { showFullDialog = false }
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.9f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "📊 Interview Analysis",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = ChatPrimaryColor
                        )
                        IconButton(onClick = { showFullDialog = false }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color.Gray
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Scrollable content
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Candidate Info
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F7FA))
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = analysis.candidateName,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Position: ${analysis.position}",
                                        fontSize = 14.sp,
                                        color = Color.Gray
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = "Completion: ${analysis.completionPercentage}%",
                                            fontSize = 12.sp
                                        )
                                        Text(
                                            text = "Duration: ${analysis.interviewDuration}",
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        }
                        
                        item { Spacer(modifier = Modifier.height(16.dp)) }
                        
                        // Overall Score
                        item {
                            val recColor = when(analysis.recommendation) {
                                "STRONG_HIRE" -> Color(0xFF00C853)
                                "HIRE" -> Color(0xFF2E7D32)
                                "MAYBE" -> Color(0xFFF9A825)
                                "NO_HIRE" -> Color(0xFFC62828)
                                else -> Color.Gray
                            }
                            
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = recColor.copy(alpha = 0.1f))
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "${analysis.overallScore}",
                                        fontSize = 48.sp,
                                        fontWeight = FontWeight.Black,
                                        color = recColor
                                    )
                                    Text(
                                        text = "Overall Score",
                                        fontSize = 14.sp,
                                        color = Color.Gray
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = analysis.recommendation.replace("_", " "),
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = recColor
                                    )
                                }
                            }
                        }
                        
                        item { Spacer(modifier = Modifier.height(16.dp)) }
                        
                        // Summary
                        item {
                            Text(
                                text = "📝 Summary",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = ChatPrimaryColor
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = analysis.summary,
                                fontSize = 14.sp,
                                lineHeight = 20.sp,
                                color = Color.DarkGray
                            )
                        }
                        
                        item { Spacer(modifier = Modifier.height(16.dp)) }
                        
                        // Strengths
                        item {
                            Text(
                                text = "✅ Strengths",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2E7D32)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        
                        items(analysis.strengths) { strength ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Text("• ", fontSize = 14.sp, color = Color(0xFF2E7D32))
                                Text(
                                    text = strength,
                                    fontSize = 14.sp,
                                    lineHeight = 20.sp
                                )
                            }
                        }
                        
                        item { Spacer(modifier = Modifier.height(16.dp)) }
                        
                        // Weaknesses
                        item {
                            Text(
                                text = "⚠️ Areas for Improvement",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFF9A825)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        
                        items(analysis.weaknesses) { weakness ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Text("• ", fontSize = 14.sp, color = Color(0xFFF9A825))
                                Text(
                                    text = weakness,
                                    fontSize = 14.sp,
                                    lineHeight = 20.sp
                                )
                            }
                        }
                        
                        item { Spacer(modifier = Modifier.height(16.dp)) }
                        
                        // Question Analysis
                        item {
                            Text(
                                text = "📋 Detailed Question Analysis",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = ChatPrimaryColor
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        
                        items(analysis.questionAnalysis) { qa ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F7FA))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "Q: ${qa.question}",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = ChatPrimaryColor
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "A: ${qa.answer}",
                                        fontSize = 13.sp,
                                        color = Color.DarkGray
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "Score: ${qa.score}/10",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (qa.score >= 7) Color(0xFF2E7D32) else Color(0xFFF9A825)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = qa.feedback,
                                        fontSize = 12.sp,
                                        color = Color.Gray,
                                        lineHeight = 16.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
