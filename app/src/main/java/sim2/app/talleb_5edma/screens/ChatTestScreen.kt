package sim2.app.talleb_5edma.screens

import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import sim2.app.talleb_5edma.models.CreateChatRequest
import sim2.app.talleb_5edma.models.MessageType
import sim2.app.talleb_5edma.models.SendMessageRequest
import sim2.app.talleb_5edma.network.ChatRepository
import sim2.app.talleb_5edma.network.determineMessageType
import sim2.app.talleb_5edma.network.getFileNameFromUri
import sim2.app.talleb_5edma.util.BASE_URL
import sim2.app.talleb_5edma.util.getToken
import java.io.File
import java.io.FileInputStream
import java.text.SimpleDateFormat
import java.util.*



@Composable
fun ChatTestScreen() {
    val repository = remember { ChatRepository() }
    val context = LocalContext.current
    val initialToken = remember { getToken(context) }
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    // --- State for Inputs ---
    var token by remember { mutableStateOf(initialToken) }
    var currentChatId by remember { mutableStateOf("") }
    var entrepriseId by remember { mutableStateOf("691fe47e27c88cf62992f7b3") }
    var offerId by remember { mutableStateOf("691fe4ba27c88cf62992f7f7") }

    // Voice recording state - FIXED: Use mutableIntStateOf and mutableLongStateOf
    var isRecording by remember { mutableStateOf(false) }
    var recordingTime by remember { mutableIntStateOf(0) }
    var mediaRecorder: MediaRecorder? by remember { mutableStateOf(null) }
    var audioFile: File? by remember { mutableStateOf(null) }
    var startTime by remember { mutableLongStateOf(0L) }

    // Audio playback state
    var isPlaying by remember { mutableStateOf(false) }
    var mediaPlayer: MediaPlayer? by remember { mutableStateOf(null) }
    var currentAudioUrl by remember { mutableStateOf<String?>(null) }
    var playbackError by remember { mutableStateOf<String?>(null) }

    // Audio recording permission
    var hasAudioPermission by remember { mutableStateOf(false) }

    // Helper to log results
    fun logResult(methodName: String, result: Any?) {
        println("ChatLog: Tests -> [$methodName] Result: $result")
    }

    fun logError(methodName: String, error: Throwable) {
        println("ChatLog: Tests -> [$methodName] Error: ${error.message}")
        error.printStackTrace()
    }

    // Audio playback functions - UPDATED with BASE_URL
    fun playAudio(audioUrl: String) {
        scope.launch {
            try {
                mediaPlayer?.release()
                mediaPlayer = null
                playbackError = null

                mediaPlayer = MediaPlayer().apply {
                    // Correct URL for emulator/device playback using BASE_URL
                    val correctedUrl = when {
                        // If it's a local file path, use as-is
                        audioUrl.startsWith("/") -> audioUrl
                        // If it's a relative URL from server response, prepend BASE_URL
                        audioUrl.startsWith("uploads/") -> {
                            "$BASE_URL/$audioUrl"
                        }
                        // If it's already a full URL but contains localhost, use BASE_URL logic
                        audioUrl.contains("localhost") && audioUrl.startsWith("http") -> {
                            audioUrl.replace("http://localhost:3005", BASE_URL)
                        }
                        // If it's a full URL with 10.0.2.2 already, use as-is
                        audioUrl.contains("10.0.2.2") -> audioUrl
                        // Otherwise use as-is
                        else -> audioUrl
                    }

                    println("ChatLog: Debug -> Original URL: $audioUrl")
                    println("ChatLog: Debug -> Corrected URL: $correctedUrl")
                    println("ChatLog: Debug -> BASE_URL: $BASE_URL")

                    setDataSource(correctedUrl)
                    setOnPreparedListener {
                        isPlaying = true
                        start()
                        playbackError = null
                        println("ChatLog: Debug -> Audio playback started successfully")
                    }
                    setOnCompletionListener {
                        isPlaying = false
                        mediaPlayer?.release()
                        mediaPlayer = null
                        println("ChatLog: Debug -> Audio playback completed")
                    }
                    setOnErrorListener { mp, what, extra ->
                        playbackError = "MediaPlayer error: what=$what, extra=$extra"
                        println("ChatLog: Error -> $playbackError")
                        isPlaying = false
                        mediaPlayer?.release()
                        mediaPlayer = null
                        true
                    }
                    prepareAsync()
                }
                currentAudioUrl = audioUrl
            } catch (e: Exception) {
                playbackError = "Failed to play audio: ${e.message}"
                logError("playAudio", e)
                isPlaying = false
            }
        }
    }

    fun stopAudio() {
        mediaPlayer?.let { player ->
            if (player.isPlaying) {
                player.stop()
            }
            player.release()
            mediaPlayer = null
        }
        isPlaying = false
        playbackError = null
    }

    // Test server connectivity with different URL formats
    fun testServerAudioPlayback() {
        scope.launch {
            try {
                // Test with the last uploaded audio URL
                currentAudioUrl?.let { url ->
                    println("ChatLog: Debug -> Testing server audio playback: $url")
                    playAudio(url)
                } ?: run {
                    playbackError = "No audio URL available for testing"
                    println("ChatLog: Error -> $playbackError")
                }
            } catch (e: Exception) {
                logError("testServerAudioPlayback", e)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    fun tryAlternativeRecording(context: Context) {
        try {
            println("ChatLog: Debug -> Trying alternative recording setup")

            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val storageDir = context.cacheDir
            audioFile = File.createTempFile("audio_${timeStamp}_", ".3gp", storageDir)

            // FIXED: MediaRecorder constructor without parameters is deprecated
            mediaRecorder = MediaRecorder(context).apply {
                setAudioSource(MediaRecorder.AudioSource.VOICE_RECOGNITION)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                setOutputFile(audioFile?.absolutePath)

                try {
                    prepare()
                    start()
                    isRecording = true
                    recordingTime = 0
                    startTime = System.currentTimeMillis()
                    println("ChatLog: Alternative recording started successfully")
                } catch (e: Exception) {
                    logError("alternativeRecording", e)
                    release()
                    mediaRecorder = null
                    isRecording = false
                }
            }
        } catch (e: Exception) {
            logError("tryAlternativeRecording", e)
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    fun startRecording(context: Context) {
        try {
            // Stop any currently playing audio
            stopAudio()

            // Release any existing recorder first
            mediaRecorder?.release()
            mediaRecorder = null

            // Create audio file
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val storageDir = context.cacheDir
            audioFile = File.createTempFile("audio_${timeStamp}_", ".mp3", storageDir)

            println("ChatLog: Debug -> Creating audio file at: ${audioFile?.absolutePath}")

            // FIXED: MediaRecorder constructor without parameters is deprecated
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
                    println("ChatLog: Recording started successfully - ${audioFile?.absolutePath}")
                    println("ChatLog: Debug -> File path set to: ${audioFile?.absolutePath}")
                } catch (e: Exception) {
                    logError("startRecording - MediaRecorder setup", e)
                    release()
                    mediaRecorder = null
                    isRecording = false

                    // Try alternative setup with 3GP format
                    tryAlternativeRecording(context)
                }
            }
        } catch (e: Exception) {
            logError("startRecording", e)
            isRecording = false
        }
    }

    fun stopRecordingAndSend() {
        try {
            mediaRecorder?.apply {
                try {
                    stop()
                } catch (e: Exception) {
                    logError("stopRecording", e)
                }
                release()
            }
            mediaRecorder = null
            isRecording = false

            // Calculate actual duration
            val actualDuration = ((System.currentTimeMillis() - startTime) / 1000).toInt()
            recordingTime = actualDuration

            // Send the recorded audio
            audioFile?.let { file ->
                // Validate file exists and has content
                if (file.exists() && file.length() > 1000) {
                    scope.launch {
                        try {
                            println("ChatLog: Debug -> Audio file found: ${file.absolutePath}")
                            println("ChatLog: Debug -> File size: ${file.length()} bytes")
                            println("ChatLog: Debug -> Actual duration: ${actualDuration}s")

                            // Read the audio file
                            val fileBytes = withContext(Dispatchers.IO) {
                                try {
                                    FileInputStream(file).use { it.readBytes() }
                                } catch (e: Exception) {
                                    logError("readAudioFile", e)
                                    null
                                }
                            }

                            if (fileBytes != null && fileBytes.isNotEmpty()) {
                                // Use .mp3 extension for backend compatibility
                                val fileName = "voice_message_${System.currentTimeMillis()}.mp3"
                                val duration = "${actualDuration}s"
                                val fileSize = fileBytes.size

                                println("ChatLog: Debug -> Audio file size: ${fileBytes.size} bytes")
                                println("ChatLog: Debug -> Audio file duration: $duration")

                                // Upload media first
                                println("ChatLog: Debug -> Uploading voice message...")
                                val uploadResponse = repository.uploadMedia(token, fileBytes, fileName)
                                logResult("uploadVoiceMessage", uploadResponse)

                                if (uploadResponse.url.isNotEmpty()) {
                                    // Store the URL for playback
                                    currentAudioUrl = uploadResponse.url

                                    // Create message request with proper file size formatting
                                    val formattedFileSize = "%.2f MB".format(fileSize / 1024.0 / 1024.0)

                                    val messageRequest = SendMessageRequest(
                                        type = MessageType.AUDIO,
                                        content = "Voice message", // Some backends require content even for media
                                        mediaUrl = uploadResponse.url,
                                        fileName = fileName,
                                        fileSize = formattedFileSize,
                                        duration = duration
                                    )

                                    println("ChatLog: Debug -> Sending message request: $messageRequest")

                                    // Send the message
                                    val messageResponse = repository.sendMessage(token, currentChatId, messageRequest)
                                    logResult("sendVoiceMessage", messageResponse)

                                    // Check if message was actually created
                                    println("ChatLog: Tests -> Voice message sent successfully!")
                                    println("ChatLog: Tests -> Message ID: ${messageResponse.id}")
                                    println("ChatLog: Tests -> Duration: $duration")
                                    println("ChatLog: Tests -> URL: ${uploadResponse.url}")
                                    println("ChatLog: Tests -> File name: $fileName")

                                } else {
                                    println("ChatLog: Error -> Upload response has empty URL")
                                }

                            } else {
                                println("ChatLog: Error -> Audio file is empty or couldn't be read")
                            }

                        } catch (e: Exception) {
                            logError("sendVoiceMessage", e)
                        }
                    }
                } else {
                    println("ChatLog: Error -> Audio file doesn't exist or is too small")
                    println("ChatLog: Debug -> File exists: ${file.exists()}, File size: ${if (file.exists()) file.length() else "N/A"} bytes")

                    // Clean up invalid file
                    scope.launch {
                        withContext(Dispatchers.IO) {
                            try {
                                if (file.exists()) {
                                    file.delete()
                                    audioFile = null
                                }
                            } catch (e: Exception) {
                                logError("cleanupInvalidFile", e)
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            logError("stopRecording", e)
        }
    }

    // Audio recording permission launcher
    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasAudioPermission = isGranted
        if (isGranted) {
            // Permission granted, start recording
            startRecording(context)
        } else {
            println("ChatLog: Audio recording permission denied")
        }
    }

    // Handle record button click
    fun handleRecordButtonClick() {
        if (isRecording) {
            stopRecordingAndSend()
        } else {
            // FIXED: Condition is always 'true' - simplified logic
            if (hasAudioPermission) {
                startRecording(context)
            } else {
                // Request permission
                audioPermissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
            }
        }
    }

    // Check permission on composition
    LaunchedEffect(Unit) {
        hasAudioPermission = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        // Log the BASE_URL for debugging
        println("ChatLog: Debug -> Using BASE_URL: $BASE_URL")
    }

    // Recording timer effect - more accurate timing
    LaunchedEffect(isRecording) {
        if (isRecording) {
            startTime = System.currentTimeMillis()
            while (isRecording) {
                delay(100)
                recordingTime = ((System.currentTimeMillis() - startTime) / 1000).toInt()
            }
        }
        // FIXED: Removed unnecessary else block that made condition always true
    }

    // Clean up MediaPlayer when composable leaves composition
    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer?.release()
            mediaRecorder?.release()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Chat Repository Tester", style = MaterialTheme.typography.headlineMedium)

        // --- Configuration Inputs ---
        OutlinedTextField(
            value = token,
            onValueChange = { token = it },
            label = { Text("Auth Token") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = currentChatId,
            onValueChange = { currentChatId = it },
            label = { Text("Current Chat ID") },
            modifier = Modifier.fillMaxWidth()
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = entrepriseId,
                onValueChange = { entrepriseId = it },
                label = { Text("Entr. ID") },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = offerId,
                onValueChange = { offerId = it },
                label = { Text("Offer ID") },
                modifier = Modifier.weight(1f)
            )
        }

        // Show BASE_URL for debugging
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Text(
                text = "BASE_URL: $BASE_URL", // FIXED: Removed redundant curly braces
                modifier = Modifier.padding(8.dp),
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }

        // FIXED: Divider deprecated - replaced with HorizontalDivider
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        // --- Voice Recording Section ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (isRecording) MaterialTheme.colorScheme.errorContainer
                else MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Voice Message",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (!hasAudioPermission) {
                    Text(
                        "Microphone permission required",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (isRecording) {
                    Text(
                        "Recording: ${recordingTime}s",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                } else if (recordingTime > 0) {
                    Text(
                        "Last recording: ${recordingTime}s",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Button(
                    onClick = { handleRecordButtonClick() },
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isRecording) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.primary
                    ),
                    // FIXED: Condition is always 'true' - simplified
                    enabled = hasAudioPermission
                ) {
                    if (isRecording) {
                        Icon(Icons.Default.Stop, "Stop Recording")
                    } else {
                        Icon(Icons.Default.Mic, "Start Recording")
                    }
                }

                if (isRecording) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Click stop to send voice message",
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                // Audio Playback Section
                if (currentAudioUrl != null && !isRecording) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Audio Playback",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )

                    // Playback controls
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = {
                                if (isPlaying) {
                                    stopAudio()
                                } else {
                                    currentAudioUrl?.let { playAudio(it) }
                                }
                            },
                            modifier = Modifier.size(56.dp)
                        ) {
                            if (isPlaying) {
                                Icon(Icons.Default.Pause, "Pause Audio")
                            } else {
                                Icon(Icons.Default.PlayArrow, "Play Audio")
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column {
                            Text(
                                "Play uploaded audio",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            if (playbackError != null) {
                                Text(
                                    "Error: $playbackError", // FIXED: Removed redundant curly braces
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }

                    // Show the audio URL for testing
                    Text(
                        "URL: $currentAudioUrl", // FIXED: Removed redundant curly braces
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    // Test server connectivity button
                    Button(
                        onClick = { testServerAudioPlayback() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Text("Test Server Audio Playback")
                    }
                }

                // Debug button to test audio file
                if (audioFile != null && !isRecording) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            scope.launch {
                                try {
                                    val file = audioFile!!
                                    println("ChatLog: Debug -> Testing audio file: ${file.absolutePath}")
                                    println("ChatLog: Debug -> File exists: ${file.exists()}")
                                    println("ChatLog: Debug -> File size: ${if (file.exists()) file.length() else "N/A"} bytes")
                                    println("ChatLog: Debug -> File extension: ${file.extension}")
                                    println("ChatLog: Debug -> File path: ${file.absolutePath}")

                                    if (file.exists()) {
                                        // Test reading the file
                                        val fileBytes = withContext(Dispatchers.IO) {
                                            FileInputStream(file).use { it.readBytes() }
                                        }
                                        println("ChatLog: Debug -> File read successfully: ${fileBytes.size} bytes")

                                        // Test playing the local file
                                        playAudio(file.absolutePath)
                                    } else {
                                        println("ChatLog: Debug -> Audio file doesn't exist at path: ${file.absolutePath}")
                                    }
                                } catch (e: Exception) {
                                    logError("testAudioFile", e)
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                    ) {
                        Text("Test Audio File Locally (Debug)")
                    }

                    // Cleanup button for audio file
                    Button(
                        onClick = {
                            scope.launch {
                                withContext(Dispatchers.IO) {
                                    try {
                                        audioFile?.let { file ->
                                            if (file.exists()) {
                                                file.delete()
                                                audioFile = null
                                                println("ChatLog: Debug -> Audio file manually cleaned up")
                                            }
                                        }
                                    } catch (e: Exception) {
                                        logError("manualCleanup", e)
                                    }
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                    ) {
                        Text("Cleanup Audio File")
                    }
                }

                // Test text message button
                Button(
                    onClick = {
                        scope.launch {
                            try {
                                // Test sending a simple text message first
                                val testRequest = SendMessageRequest(
                                    type = MessageType.TEXT,
                                    content = "Test message at ${System.currentTimeMillis()}"
                                )
                                val testResponse = repository.sendMessage(token, currentChatId, testRequest)
                                println("ChatLog: Debug -> Test text message response: $testResponse")

                                println("ChatLog: Debug -> Text message works fine")
                            } catch (e: Exception) {
                                logError("testTextMessage", e)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Text("Test Text Message Sending (Debug)")
                }
            }
        }

        // FIXED: Divider deprecated - replaced with HorizontalDivider
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        // --- 1. Create or Get Chat ---
        Button(
            onClick = {
                scope.launch {
                    try {
                        val request = CreateChatRequest(entreprise = entrepriseId, offer = offerId)
                        val response = repository.createOrGetChat(token, request)
                        logResult("createOrGetChat", response)
                        currentChatId = response.id
                    } catch (e: Exception) { logError("createOrGetChat", e) }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("1. Create or Get Chat") }

        // --- 2. Send Message (Text) ---
        Button(
            onClick = {
                scope.launch {
                    try {
                        val request = SendMessageRequest(
                            type = MessageType.TEXT,
                            content = "Hello from Test Screen! ${System.currentTimeMillis()}"
                        )
                        val response = repository.sendMessage(token, currentChatId, request)
                        logResult("sendMessage", response)
                    } catch (e: Exception) { logError("sendMessage", e) }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("2. Send Message (Text)") }

        // --- 3. Get User Chats ---
        Button(
            onClick = {
                scope.launch {
                    try {
                        val response = repository.getMyChats(token)
                        logResult("getUserChats", "Found ${response.size} chats")
                        response.forEach { println("ChatLog: Tests -> Chat: ${it.id} with ${it.entreprise?.nom}") }
                    } catch (e: Exception) { logError("getUserChats", e) }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("3. Get My Chats") }

        // --- 4. Get Chat Messages ---
        Button(
            onClick = {
                scope.launch {
                    try {
                        val response = repository.getMessages(token, currentChatId)
                        logResult("getChatMessages", "Total: ${response.total}, Count: ${response.messages.size}")
                        response.messages.forEach { message ->
                            println("ChatLog: Tests -> Message: ${message.content} from ${message.sender?.nom}")
                        }
                    } catch (e: Exception) { logError("getChatMessages", e) }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("4. Get Messages") }

        // --- 5. Get Chat By ID ---
        Button(
            onClick = {
                scope.launch {
                    try {
                        val response = repository.getChatById(token, currentChatId)
                        logResult("getChatById", response)
                        println("ChatLog: Tests -> Chat details - Candidate: ${response.candidate?.nom}, Entreprise: ${response.entreprise?.nom}, Offer: ${response.offer?.title}")
                    } catch (e: Exception) { logError("getChatById", e) }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("5. Get Chat By ID") }

        // --- 6. Can Make Call ---
        Button(
            onClick = {
                scope.launch {
                    try {
                        val canCall = repository.canMakeCall(token, offerId)
                        logResult("canMakeCall", "Can call: $canCall")
                    } catch (e: Exception) { logError("canMakeCall", e) }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("6. Check Can Call") }

        // --- 7. Mark Read ---
        Button(
            onClick = {
                scope.launch {
                    try {
                        val response = repository.markMessagesAsRead(token, currentChatId)
                        logResult("markMessagesAsRead", response)
                    } catch (e: Exception) { logError("markMessagesAsRead", e) }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("7. Mark Messages Read") }

        // --- 8. Upload Media (Images & Videos Only) ---
        var isUploading by remember { mutableStateOf(false) }

        val pickMultipleMedia = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.PickMultipleVisualMedia()
        ) { uris: List<Uri> ->
            if (uris.isNotEmpty()) {
                scope.launch {
                    isUploading = true
                    try {
                        for (uri in uris) {
                            val fileName = getFileNameFromUri(context, uri) ?: "uploaded_file"
                            println("ChatLog: Debug -> Processing file: $fileName")

                            val fileBytes = withContext(Dispatchers.IO) {
                                context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                                    ?: throw Exception("Could not read file from $uri")
                            }

                            val fileSize = "%.2f MB".format(fileBytes.size / 1024.0 / 1024.0)

                            println("ChatLog: Debug -> Uploading file: $fileName")
                            val uploadResponse = repository.uploadMedia(token, fileBytes, fileName)
                            logResult("uploadMedia", uploadResponse)

                            val messageType = determineMessageType(fileName)

                            println("ChatLog: Debug -> Sending message for file: $fileName")
                            val messageRequest = SendMessageRequest(
                                type = messageType,
                                content = fileName,
                                mediaUrl = uploadResponse.url,
                                fileName = fileName,
                                fileSize = fileSize,
                                duration = null
                            )

                            val messageResponse = repository.sendMessage(token, currentChatId, messageRequest)
                            logResult("sendFileMessage", messageResponse)

                            println("ChatLog: Tests -> File uploaded and sent successfully!")
                            println("ChatLog: Tests -> Type: $messageType")
                            println("ChatLog: Tests -> URL: ${uploadResponse.url}")
                        }

                        println("ChatLog: Tests -> All ${uris.size} files uploaded and sent as messages successfully!")

                    } catch (e: Exception) {
                        logError("uploadMedia", e)
                    } finally {
                        isUploading = false
                    }
                }
            }
        }

        Button(
            onClick = {
                pickMultipleMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo))
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isUploading
        ) {
            if (isUploading) {
                CircularProgressIndicator(
                    color = Color.White,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Uploading...")
            } else {
                Text("Select & Upload Images or Videos")
            }
        }

        // FIXED: Divider deprecated - replaced with HorizontalDivider
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        Text("Entreprise Only Actions", style = MaterialTheme.typography.titleSmall)

        // --- 9. Accept Candidate ---
        Button(
            onClick = {
                scope.launch {
                    try {
                        val response = repository.acceptCandidate(token, currentChatId)
                        logResult("acceptCandidate", response)
                    } catch (e: Exception) { logError("acceptCandidate", e) }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
        ) { Text("9. Accept Candidate") }

        // --- 10. Block Chat ---
        Button(
            onClick = {
                scope.launch {
                    try {
                        val response = repository.blockChat(token, currentChatId, "Test block reason")
                        logResult("blockChat", response)
                    } catch (e: Exception) { logError("blockChat", e) }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) { Text("10. Block Chat") }

        // --- 11. Unblock Chat ---
        Button(
            onClick = {
                scope.launch {
                    try {
                        val response = repository.unblockChat(token, currentChatId)
                        logResult("unblockChat", response)
                    } catch (e: Exception) { logError("unblockChat", e) }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
        ) { Text("11. Unblock Chat") }

        // --- 12. Delete Chat ---
        Button(
            onClick = {
                scope.launch {
                    try {
                        val response = repository.deleteChat(token, currentChatId)
                        logResult("deleteChat", response)
                    } catch (e: Exception) { logError("deleteChat", e) }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) { Text("12. Delete Chat") }
    }
}