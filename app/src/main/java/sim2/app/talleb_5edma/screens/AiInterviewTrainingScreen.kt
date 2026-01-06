package sim2.app.talleb_5edma.screens

import android.annotation.SuppressLint
import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.util.Base64
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import sim2.app.talleb_5edma.models.AiMessage
import sim2.app.talleb_5edma.models.User
import sim2.app.talleb_5edma.network.AiChatRepository
import sim2.app.talleb_5edma.network.ChatRepository
import sim2.app.talleb_5edma.network.InterviewAnalysisRepository
import sim2.app.talleb_5edma.network.OffreRepository
import sim2.app.talleb_5edma.network.UserRepository
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Color theme matching the app
private val AiPrimaryColor = Color(0xFF7C4DFF)
private val AiAccentColor = Color(0xFFD81B60)
private val AiSurfaceColor = Color(0xFFFFF0F5)
private val AiBubbleUser = Color(0xFFD81B60)
private val AiBubbleAi = Color(0xFFF5F5F5)

@Composable
fun AiInterviewTrainingScreen(
    navController: NavController,
    chatId: String,
    token: String,
    initialMode: String = "coaching" // Default to coaching
) {
    //println("Ai_catlog: AiInterviewTrainingScreen - Initializing with chatId: $chatId")
    
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // Repositories
    val aiChatRepository = remember { AiChatRepository() }
    val userRepository = remember { UserRepository() }
    val chatRepository = remember { ChatRepository() }
    val offreRepository = remember { OffreRepository() }
    
    // State
    var messages by remember { mutableStateOf<List<AiMessage>>(emptyList()) }
    var textInput by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    
    // Voice recording state
    var isRecording by remember { mutableStateOf(false) }
    var recordingTime by remember { mutableIntStateOf(0) }
    var mediaRecorder: MediaRecorder? by remember { mutableStateOf(null) }
    var audioFile: File? by remember { mutableStateOf(null) }
    var startTime by remember { mutableLongStateOf(0L) }
    
    // Interview timer state (10 minutes)
    var interviewTimeRemaining by remember { mutableIntStateOf(600) } // 600 seconds = 10 min
    var isTimerVisible by remember { mutableStateOf(true) }
    var isInterviewActive by remember { mutableStateOf(false) }
    var interviewStartTime by remember { mutableLongStateOf(0L) }
    
    // Audio playback state
    var mediaPlayer: MediaPlayer? by remember { mutableStateOf(null) }
    var currentlyPlayingId by remember { mutableStateOf<String?>(null) }
    
    // TextToSpeech for AI voice greeting
    var textToSpeech: android.speech.tts.TextToSpeech? by remember { mutableStateOf(null) }
    var isTtsReady by remember { mutableStateOf(false) }
    
    // Initialize TTS
    DisposableEffect(Unit) {
        textToSpeech = android.speech.tts.TextToSpeech(context) { status ->
            if (status == android.speech.tts.TextToSpeech.SUCCESS) {
                textToSpeech?.language = Locale.US
                isTtsReady = true
                println("Ai_catlog: AiInterviewTrainingScreen - TTS initialized successfully")
            } else {
                println("Ai_catlog: AiInterviewTrainingScreen - TTS initialization failed")
            }
        }
        
        onDispose {
            println("Ai_catlog: AiInterviewTrainingScreen - Disposing TTS")
            textToSpeech?.stop()
            textToSpeech?.shutdown()
            textToSpeech = null
        }
    }
    
    // Pending auto-play (for greeting voice)
    var pendingAutoPlayAudio by remember { mutableStateOf<Pair<ByteArray, String>?>(null) }
    var pendingTtsGreeting by remember { mutableStateOf<String?>(null) }
    
    // Session and data
    val sessionId = remember { "session_${System.currentTimeMillis()}_$chatId" }
    var offerPosition by remember { mutableStateOf<String?>(null) }
    var companyName by remember { mutableStateOf<String?>(null) }
    var userDetailsMap by remember { mutableStateOf<Map<String, Any>>(emptyMap()) }
    var offerDetailsMap by remember { mutableStateOf<Map<String, Any>>(emptyMap()) }
    
    // AI Mode derived from parameter
    val isCoachingMode = initialMode == "coaching"
    val currentMode = initialMode
    
    val listState = rememberLazyListState()
    
    //println("Ai_catlog: AiInterviewTrainingScreen - Session ID: $sessionId")
    
    // Load user and offer data in background
    LaunchedEffect(chatId) {
        println("Ai_catlog: AiInterviewTrainingScreen - Loading background data...")
        
        withContext(Dispatchers.IO) {
            try {
                // Load current user
                println("Ai_catlog: AiInterviewTrainingScreen - Fetching user details...")
                val userResponse = userRepository.getCurrentUser(token)
                val user = userResponse.data ?: User(
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
                    likedOffres = null,
                    cvExperience = userResponse.cvExperience,
                    cvEducation = userResponse.cvEducation,
                    cvSkills = userResponse.cvSkills
                )
                
                println("Ai_catlog: AiInterviewTrainingScreen - User: ${user.nom}, Email: ${user.email}")
                
                // Load chat to get offer details
                println("Ai_catlog: AiInterviewTrainingScreen - Fetching chat details for chatId: $chatId")
                val chat = chatRepository.getChatById(token, chatId)
                
                // Get offer details
                val offerId = chat.offer?.id
                println("Ai_catlog: AiInterviewTrainingScreen - Offer ID: $offerId")
                
                val offer = if (offerId != null) {
                    try {
                        offreRepository.getOffreById(offerId)
                    } catch (e: Exception) {
                        println("Ai_catlog: AiInterviewTrainingScreen - Error fetching offer: ${e.message}")
                        null
                    }
                } else null
                
                println("Ai_catlog: AiInterviewTrainingScreen - Offer: ${offer?.title}, Company: ${offer?.company}")
                
                // Get the actual user name - try multiple sources
                val userName = user.nom ?: userResponse.nom
                println("Ai_catlog: AiInterviewTrainingScreen - Resolved User Name: $userName")
                
                // Logic to extract experience from CV or fallback to role
                val experience = if (!user.cvExperience.isNullOrEmpty()) {
                    user.cvExperience.joinToString(", ")
                } else if (!userResponse.cvExperience.isNullOrEmpty()) {
                    userResponse.cvExperience.joinToString(", ")
                } else {
                    user.role ?: userResponse.role
                }

                val education = if (!user.cvEducation.isNullOrEmpty()) {
                    user.cvEducation.joinToString(", ")
                } else if (!userResponse.cvEducation.isNullOrEmpty()) {
                    userResponse.cvEducation.joinToString(", ")
                } else {
                    "Not specified"
                }

                val skills = if (!user.cvSkills.isNullOrEmpty()) {
                    user.cvSkills
                } else if (!userResponse.cvSkills.isNullOrEmpty()) {
                    userResponse.cvSkills
                } else {
                    emptyList()
                }
                
                withContext(Dispatchers.Main) {
                    offerPosition = offer?.title ?: "Position"
                    companyName = offer?.company ?: "Company"
                    
                    // Build user details map with proper fallbacks
                    userDetailsMap = mapOf(
                        "name" to userName,
                        "experience_level" to experience,
                        "education" to education,
                        "skills" to skills,
                        "country" to "Tunisia",
                        "languages" to listOf("Arabic", "French", "English")
                    )
                    
                    // Build offer details map
                    offerDetailsMap = mapOf(
                        "position" to (offer?.title ?: "Position"),
                        "company" to (offer?.company ?: "Company"),
                        "required_skills" to (offer?.tags ?: emptyList()),
                        "salary_range" to (offer?.salary ?: "Not specified"),
                        "location" to (offer?.location?.address ?: "Tunisia")
                    )
                    
                    println("Ai_catlog: AiInterviewTrainingScreen - User details: $userDetailsMap")
                    println("Ai_catlog: AiInterviewTrainingScreen - Offer details: $offerDetailsMap")
                }
                
                // Generate welcome message with voice
                val welcomeText = if (isCoachingMode) {
                    "Welcome to AI Interview Training! I'm your personal interview coach. I'll help you prepare for your interview at ${offer?.company ?: "the company"} for the ${offer?.title ?: "position"}. You can use voice or text to practice. Let's begin!"
                } else {
                    "Hello $userName, welcome to your mock interview! I'm the interviewer from ${offer?.company ?: "the company"}. I'll be conducting your interview for the ${offer?.title ?: "position"} position. Please introduce yourself and tell me about your background."
                }
                
                // Get AI greeting from API (with voice)
                println("Ai_catlog: AiInterviewTrainingScreen - Generating AI greeting...")
                try {
                    val greetingPrompt = if (isCoachingMode) {
                        "Greet the candidate named $userName who is preparing for an interview at ${offer?.company ?: "a company"} for the ${offer?.title ?: "a position"} position. Be encouraging and ask how you can help them prepare."
                    } else {
                        "You are the interviewer. Greet the candidate named $userName professionally and introduce yourself as the interviewer for the ${offer?.title ?: "position"} position at ${offer?.company ?: "the company"}. Then ask them to introduce themselves."
                    }
                    
                    val greetingResponse = aiChatRepository.sendTextMessage(
                        text = greetingPrompt,
                        sessionId = sessionId,
                        mode = currentMode,
                        userDetails = userDetailsMap,
                        offerDetails = offerDetailsMap,
                        chatHistory = emptyList()
                    )
                    
                    if (greetingResponse.success && greetingResponse.ai_response != null) {
                        withContext(Dispatchers.Main) {
                            val greetingMessage = AiMessage(
                                text = greetingResponse.ai_response,
                                isUser = false,
                                audioData = null // No audio from backend
                            )
                            messages = listOf(greetingMessage)
                            
                            // Queue TTS for auto-play
                            println("Ai_catlog: AiInterviewTrainingScreen - Queuing greeting for TTS...")
                            pendingTtsGreeting = greetingResponse.ai_response
                        }
                    } else {
                        // Fallback to text welcome
                        println("Ai_catlog: AiInterviewTrainingScreen - API failed, using text fallback")
                        withContext(Dispatchers.Main) {
                            messages = listOf(AiMessage(text = welcomeText, isUser = false))
                        }
                    }
                } catch (e: Exception) {
                    println("Ai_catlog: AiInterviewTrainingScreen - Greeting failed: ${e.message}")
                    withContext(Dispatchers.Main) {
                        messages = listOf(AiMessage(text = welcomeText, isUser = false))
                    }
                }
                
            } catch (e: Exception) {
                println("Ai_catlog: AiInterviewTrainingScreen - Error loading data: ${e.message}")
                e.printStackTrace()
                
                withContext(Dispatchers.Main) {
                    error = "Failed to load data: ${e.message}"
                }
            }
        }
    }
    
    // Recording timer
    LaunchedEffect(isRecording) {
        if (isRecording) {
            startTime = System.currentTimeMillis()
            while (isRecording) {
                delay(100)
                recordingTime = ((System.currentTimeMillis() - startTime) / 1000).toInt()
            }
        }
    }
    
    // Function to trigger interview analysis (defined early to be available in LaunchedEffects)
    fun triggerInterviewAnalysis() {
        if (initialMode != "employer_interview" || !isInterviewActive) return
        
        // Use GlobalScope to survive screen disposal (fire-and-forget background task)
        kotlinx.coroutines.GlobalScope.launch(Dispatchers.IO) {
            try {
                println("🔍 Triggering interview analysis...")
                val analysisRepo = InterviewAnalysisRepository()
                val actualDuration = (System.currentTimeMillis() - interviewStartTime) / 1000
                        
                val response = analysisRepo.analyzeInterview(
                    sessionId = sessionId,
                    chatId = chatId,
                    userDetails = userDetailsMap,
                    offerDetails = offerDetailsMap,
                    durationSeconds = actualDuration.toInt()
                )
                
                if (response.success) {
                    println("✅ Interview completed - Score: ${response.analysis?.overallScore}")
                } else {
                    println("❌ Analysis failed: ${response.error}")
                }
            } catch (e: Exception) {
                println("❌ Analysis exception: ${e.message}")
                e.printStackTrace()
            }
        }
    }
    
    // Auto-start timer for employer_interview mode
    LaunchedEffect(initialMode) {
        if (initialMode == "employer_interview" && !isInterviewActive) {
            delay(500) // Small delay to ensure screen is ready
            isInterviewActive = true
            interviewStartTime = System.currentTimeMillis()
        }
    }

    // Interview timer countdown
    LaunchedEffect(isInterviewActive, interviewTimeRemaining) {
        if (isInterviewActive && interviewTimeRemaining > 0) {
            delay(1000)
            interviewTimeRemaining -= 1
        } else if (isInterviewActive && interviewTimeRemaining == 0) {
            isInterviewActive = false
            // End interview and trigger analysis
            triggerInterviewAnalysis()
            scope.launch {
                delay(2000) // Wait for analysis to complete
                withContext(Dispatchers.Main) {
                    navController.popBackStack()
                }
            }
        }
    }
    
    // Auto-scroll to bottom
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            delay(100)
            listState.animateScrollToItem(messages.size - 1)
        }
    }
    
    // Cleanup
    DisposableEffect(Unit) {
        onDispose {
            println("Ai_catlog: AiInterviewTrainingScreen - Cleaning up resources...")
            mediaPlayer?.release()
            mediaRecorder?.release()
        }
    }
    
    // Functions
    fun stopAudioPlayback() {
        mediaPlayer?.let { player ->
            if (player.isPlaying) {
                player.stop()
            }
            player.release()
            mediaPlayer = null
        }
        currentlyPlayingId = null
    }
    
    fun playAudioFromBytes(audioBytes: ByteArray, messageId: String) {
        println("Ai_catlog: AiInterviewTrainingScreen.playAudioFromBytes - Audio size: ${audioBytes.size} bytes")
        
        stopAudioPlayback()
        
        scope.launch {
            try {
                // Write to temp file
                val tempFile = File.createTempFile("ai_response_", ".mp3", context.cacheDir)
                withContext(Dispatchers.IO) {
                    tempFile.writeBytes(audioBytes)
                }
                
                println("Ai_catlog: AiInterviewTrainingScreen.playAudioFromBytes - Temp file: ${tempFile.absolutePath}")
                
                mediaPlayer?.release()
                mediaPlayer = null
                
                mediaPlayer = MediaPlayer().apply {
                    setDataSource(tempFile.absolutePath)
                    setOnPreparedListener {
                        println("Ai_catlog: AiInterviewTrainingScreen.playAudioFromBytes - Starting playback")
                        start()
                        currentlyPlayingId = messageId
                    }
                    setOnCompletionListener {
                        println("Ai_catlog: AiInterviewTrainingScreen.playAudioFromBytes - Playback completed")
                        currentlyPlayingId = null
                        tempFile.delete()
                        mediaPlayer?.release()
                        mediaPlayer = null
                    }
                    setOnErrorListener { _, what, extra ->
                        println("Ai_catlog: AiInterviewTrainingScreen.playAudioFromBytes - Error: what=$what, extra=$extra")
                        currentlyPlayingId = null
                        tempFile.delete()
                        mediaPlayer?.release()
                        mediaPlayer = null
                        true
                    }
                    prepareAsync()
                }
            } catch (e: Exception) {
                println("Ai_catlog: AiInterviewTrainingScreen.playAudioFromBytes - Exception: ${e.message}")
                e.printStackTrace()
                currentlyPlayingId = null
            }
        }
    }
    
    // Auto-play pending audio (for greeting voice)
    LaunchedEffect(pendingAutoPlayAudio) {
        pendingAutoPlayAudio?.let { (audioData, messageId) ->
            println("Ai_catlog: AiInterviewTrainingScreen - Auto-playing pending audio...")
            delay(500) // Small delay for UI to settle
            playAudioFromBytes(audioData, messageId)
            pendingAutoPlayAudio = null
        }
    }
    
    // Auto-play TTS greeting
    LaunchedEffect(pendingTtsGreeting, isTtsReady) {
        if (isTtsReady && pendingTtsGreeting != null) {
            println("Ai_catlog: AiInterviewTrainingScreen - Speaking greeting with TTS...")
            delay(800) // Small delay for UI to settle
            
            textToSpeech?.speak(
                pendingTtsGreeting,
                android.speech.tts.TextToSpeech.QUEUE_FLUSH,
                null,
                "greeting_${System.currentTimeMillis()}"
            )
            
            pendingTtsGreeting = null
        }
    }
    
    @RequiresApi(Build.VERSION_CODES.S)
    fun startRecording(context: Context) {
        println("Ai_catlog: AiInterviewTrainingScreen.startRecording - Starting...")
        
        try {
            stopAudioPlayback()
            
            mediaRecorder?.release()
            mediaRecorder = null
            
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val storageDir = context.cacheDir
            audioFile = File.createTempFile("ai_voice_${timeStamp}_", ".mp3", storageDir)
            
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
                    
                    println("Ai_catlog: AiInterviewTrainingScreen.startRecording - Recording started successfully")
                } catch (e: Exception) {
                    println("Ai_catlog: AiInterviewTrainingScreen.startRecording - Error: ${e.message}")
                    e.printStackTrace()
                    release()
                    mediaRecorder = null
                    isRecording = false
                }
            }
        } catch (e: Exception) {
            println("Ai_catlog: AiInterviewTrainingScreen.startRecording - Exception: ${e.message}")
            e.printStackTrace()
            isRecording = false
        }
    }
    
    fun stopRecordingAndSend() {
        println("Ai_catlog: AiInterviewTrainingScreen.stopRecordingAndSend - Stopping recording...")
        
        try {
            mediaRecorder?.apply {
                try { stop() } catch (e: Exception) { 
                    println("Ai_catlog: AiInterviewTrainingScreen.stopRecordingAndSend - Stop error: ${e.message}")
                }
                release()
            }
            mediaRecorder = null
            isRecording = false
            
            val actualDuration = ((System.currentTimeMillis() - startTime) / 1000).toInt()
            recordingTime = actualDuration
            
            audioFile?.let { file ->
                println("Ai_catlog: AiInterviewTrainingScreen.stopRecordingAndSend - File size: ${file.length()} bytes")
                
                if (file.exists() && file.length() > 1000) {
                    scope.launch {
                        isLoading = true
                        
                        try {
                            println("Ai_catlog: AiInterviewTrainingScreen.stopRecordingAndSend - Sending to backend...")
                            
                            val response = aiChatRepository.sendVoiceMessage(
                                audioFile = file,
                                sessionId = sessionId,
                                mode = currentMode,
                                userDetails = userDetailsMap,
                                offerDetails = offerDetailsMap
                            )
                            
                            if (response.success) {
                                println("Ai_catlog: AiInterviewTrainingScreen.stopRecordingAndSend - Success!")
                                
                                // Add user message with transcribed text
                                val userMessage = AiMessage(
                                    text = "🎤 ${response.transcribed_text}",
                                    isUser = true,
                                    audioUrl = file.absolutePath
                                )
                                
                                messages = messages + userMessage
                                
                                // Add AI response
                                if (response.ai_response != null) {
                                    val audioBytes = response.audio_response?.let { base64 ->
                                        try {
                                            Base64.decode(base64, Base64.DEFAULT)
                                        } catch (e: Exception) {
                                            println("Ai_catlog: AiInterviewTrainingScreen.stopRecordingAndSend - Base64 decode error: ${e.message}")
                                            null
                                        }
                                    }
                                    
                                    val aiMessage = AiMessage(
                                        text = response.ai_response,
                                        isUser = false,
                                        audioData = audioBytes
                                    )
                                    
                                    messages = messages + aiMessage
                                    
                                    // Auto-play AI response
                                    if (audioBytes != null) {
                                        delay(500)
                                        playAudioFromBytes(audioBytes, aiMessage.id)
                                    }
                                }
                            } else {
                                println("Ai_catlog: AiInterviewTrainingScreen.stopRecordingAndSend - Error: ${response.error}")
                                error = response.error
                            }
                        } catch (e: Exception) {
                            println("Ai_catlog: AiInterviewTrainingScreen.stopRecordingAndSend - Exception: ${e.message}")
                            e.printStackTrace()
                            error = "Failed to send voice: ${e.message}"
                        } finally {
                            isLoading = false
                        }
                    }
                } else {
                    println("Ai_catlog: AiInterviewTrainingScreen.stopRecordingAndSend - File too small or doesn't exist")
                }
            }
        } catch (e: Exception) {
            println("Ai_catlog: AiInterviewTrainingScreen.stopRecordingAndSend - Exception: ${e.message}")
            e.printStackTrace()
            isRecording = false
        }
    }
    
    fun sendTextMessage() {
        if (textInput.isBlank()) return
        
        println("Ai_catlog: AiInterviewTrainingScreen.sendTextMessage - Text: $textInput")
        
        val messageText = textInput
        textInput = ""
        
        scope.launch {
            isLoading = true
            
            try {
                // Add user message
                val userMessage = AiMessage(
                    text = messageText,
                    isUser = true
                )
                messages = messages + userMessage
                
                // Build chat history
                val chatHistory = messages.takeLast(10).windowed(2, 2, false)
                    .filter { it.size == 2 && it[0].isUser && !it[1].isUser }
                    .map { listOf(it[0].text, it[1].text) }
                
                println("Ai_catlog: AiInterviewTrainingScreen.sendTextMessage - Sending to backend...")
                
                val response = aiChatRepository.sendTextMessage(
                    text = messageText,
                    sessionId = sessionId,
                    mode = currentMode,
                    userDetails = userDetailsMap,
                    offerDetails = offerDetailsMap,
                    chatHistory = chatHistory
                )
                
                if (response.success && response.ai_response != null) {
                    println("Ai_catlog: AiInterviewTrainingScreen.sendTextMessage - Success!")
                    
                    val audioBytes = response.audio_response?.let { base64 ->
                        try {
                            Base64.decode(base64, Base64.DEFAULT)
                        } catch (e: Exception) {
                            println("Ai_catlog: AiInterviewTrainingScreen.sendTextMessage - Base64 decode error: ${e.message}")
                            null
                        }
                    }
                    
                    val aiMessage = AiMessage(
                        text = response.ai_response,
                        isUser = false,
                        audioData = audioBytes
                    )
                    
                    messages = messages + aiMessage
                    
                    // Auto-play AI response
                    if (audioBytes != null) {
                        delay(500)
                        playAudioFromBytes(audioBytes, aiMessage.id)
                    }
                } else {
                    println("Ai_catlog: AiInterviewTrainingScreen.sendTextMessage - Error: ${response.error}")
                    error = response.error
                }
            } catch (e: Exception) {
                println("Ai_catlog: AiInterviewTrainingScreen.sendTextMessage - Exception: ${e.message}")
                e.printStackTrace()
                error = "Failed to send text: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }
    
    // UI
    Scaffold(
        topBar = {
            AiTopBar(
                onBack = { 
                    // Trigger analysis before leaving if interview was active
                    if (initialMode == "employer_interview" && isInterviewActive) {
                        triggerInterviewAnalysis()
                        // Small delay to allow API call to initiate
                        scope.launch {
                            delay(500)
                            withContext(Dispatchers.Main) {
                                navController.popBackStack()
                            }
                        }
                    } else {
                        navController.popBackStack()
                    }
                },
                offerPosition = offerPosition,
                companyName = companyName,
                mode = initialMode // Pass mode for display
            )
        },
        bottomBar = {
            AiInputArea(
                isRecording = isRecording,
                recordingTime = recordingTime,
                onStartRecording = { startRecording(context) },
                onStopRecording = { stopRecordingAndSend() },
                textInput = textInput,
                onTextChange = { textInput = it },
                onSendText = { sendTextMessage() },
                isLoading = isLoading
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(AiSurfaceColor)
        ) {
            // Main content with padding for topBar and bottomBar
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Timer Bar (if interview mode and active)
                if (initialMode == "employer_interview") {
                    InterviewTimerBar(
                        timeRemaining = interviewTimeRemaining,
                        isVisible = isTimerVisible,
                        isActive = isInterviewActive,
                        onToggleVisibility = { isTimerVisible = !isTimerVisible },
                        onStartInterview = { 
                            isInterviewActive = true 
                            interviewTimeRemaining = 600
                        }
                    )
                }
                
                // Messages content
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = if (initialMode == "employer_interview" && isTimerVisible) 56.dp else 0.dp)
                        .background(AiSurfaceColor)
                ) {
            if (messages.isEmpty() && !isLoading) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "🎙️",
                        fontSize = 64.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "AI Interview Coach",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = AiPrimaryColor
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Tap the microphone to start practicing",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                AiMessageList(
                    messages = messages,
                    isLoading = isLoading,
                    listState = listState,
                    currentlyPlayingId = currentlyPlayingId,
                    onPlayAudio = { audioBytes, messageId ->
                        playAudioFromBytes(audioBytes, messageId)
                    },
                    onStopAudio = { stopAudioPlayback() }
                )
            }
            
            // Error snackbar
            error?.let { errorMessage ->
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    action = {
                        TextButton(onClick = { error = null }) {
                            Text("Dismiss")
                        }
                    }
                ) {
                    Text(errorMessage)
                }
                }
}
        }
    }
}
}


@Composable
fun AiTopBar(
    onBack: () -> Unit,
    offerPosition: String?,
    companyName: String?,
    mode: String // "coaching" or "employer_interview"
) {
    val isCoaching = mode == "coaching"
    Surface(
        color = AiPrimaryColor,
        shadowElevation = 4.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(70.dp)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                
                Spacer(Modifier.width(12.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isCoaching) "🧠 AI Interview Coach" else "👔 Mock Interview",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    if (offerPosition != null && companyName != null) {
                        Text(
                            text = "$offerPosition at $companyName",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 12.sp
                        )
                    }
                }
            }
            
            }
        }
    }


@Composable
private fun AiMessageList(
    messages: List<AiMessage>,
    isLoading: Boolean,
    listState: androidx.compose.foundation.lazy.LazyListState,
    currentlyPlayingId: String?,
    onPlayAudio: (ByteArray, String) -> Unit,
    onStopAudio: () -> Unit
) {
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(messages) { message ->
            AiMessageBubble(
                message = message,
                currentlyPlayingId = currentlyPlayingId,
                onPlayAudio = onPlayAudio,
                onStopAudio = onStopAudio
            )
        }
        
        if (isLoading) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start
                ) {
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = AiBubbleAi,
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = AiPrimaryColor,
                                strokeWidth = 2.dp
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(
                                text = "AI is thinking...",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AiMessageBubble(
    message: AiMessage,
    currentlyPlayingId: String?,
    onPlayAudio: (ByteArray, String) -> Unit,
    onStopAudio: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = if (message.isUser) AiBubbleUser else AiBubbleAi,
            modifier = Modifier
                .widthIn(max = 280.dp)
                .padding(if (message.isUser) PaddingValues(start = 48.dp) else PaddingValues(end = 48.dp))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = message.text,
                    color = if (message.isUser) Color.White else Color.Black,
                    fontSize = 14.sp
                )
                
                // Audio playback button for AI messages
                if (!message.isUser && message.audioData != null) {
                    Spacer(Modifier.height(8.dp))
                    
                    val isPlaying = currentlyPlayingId == message.id
                    
                    IconButton(
                        onClick = {
                            if (isPlaying) {
                                onStopAudio()
                            } else {
                                onPlayAudio(message.audioData, message.id)
                            }
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Stop" else "Play",
                            tint = AiPrimaryColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AiInputArea(
    isRecording: Boolean,
    recordingTime: Int,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit,
    textInput: String,
    onTextChange: (String) -> Unit,
    onSendText: () -> Unit,
    isLoading: Boolean
) {
    Surface(
        color = Color.White,
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            if (isRecording) {
                // Recording indicator
                RecordingIndicator(recordingTime = recordingTime)
                Spacer(Modifier.height(12.dp))
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Text input
                OutlinedTextField(
                    value = textInput,
                    onValueChange = onTextChange,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Type or use voice...") },
                    enabled = !isRecording && !isLoading,
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AiPrimaryColor,
                        unfocusedBorderColor = Color.Gray.copy(alpha = 0.3f)
                    ),
                    maxLines = 3
                )
                
                // Send text button
                if (textInput.isNotBlank()) {
                    FloatingActionButton(
                        onClick = onSendText,
                        containerColor = AiAccentColor,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            tint = Color.White
                        )
                    }
                } else {
                    // Voice button
                    FloatingActionButton(
                        onClick = {
                            if (isRecording) {
                                onStopRecording()
                            } else {
                                onStartRecording()
                            }
                        },
                        containerColor = if (isRecording) Color.Red else AiPrimaryColor,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                            contentDescription = if (isRecording) "Stop" else "Record",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}

@SuppressLint("DefaultLocale")
@Composable
private fun RecordingIndicator(recordingTime: Int) {
    // Pulsing animation
    val infiniteTransition = rememberInfiniteTransition(label = "recording")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(Color.Red.copy(alpha = alpha))
        )
        
        Spacer(Modifier.width(8.dp))
        
        Text(
            text = "Recording ${String.format("%02d:%02d", recordingTime / 60, recordingTime % 60)}",
            color = Color.Red,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
    }
}

// Interview Timer Bar Component
@Composable
private fun InterviewTimerBar(
    timeRemaining: Int,
    isVisible: Boolean,
    isActive: Boolean,
    onToggleVisibility: () -> Unit,
    onStartInterview: () -> Unit
) {
    Column {
        // Always show toggle button (even when hidden)
        if (!isVisible) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp)
                    .clickable { onToggleVisibility() },
                color = AiPrimaryColor.copy(alpha = 0.8f)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowDownward,
                        contentDescription = "Show timer",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
        
        AnimatedVisibility(
            visible = isVisible,
            enter = androidx.compose.animation.slideInVertically() + androidx.compose.animation.fadeIn(),
            exit = androidx.compose.animation.slideOutVertically() + androidx.compose.animation.fadeOut()
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                color = if (timeRemaining < 120 && isActive) Color(0xFFFF5722) else AiPrimaryColor,
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Timer display
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val minutes = timeRemaining / 60
                        val seconds = timeRemaining % 60
                        Text(
                            text = String.format("%d:%02d remaining", minutes, seconds),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        
                        if (timeRemaining < 120) {
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "⚠️",
                                fontSize = 20.sp
                            )
                        }
                    }
                    
                    // Hide toggle button
                    IconButton(
                        onClick = onToggleVisibility,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowUpward,
                            contentDescription = "Hide timer",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}
