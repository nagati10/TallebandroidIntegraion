package sim2.app.talleb_5edma.network

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.MediaPlayer
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Handler
import android.os.Looper
import android.util.Base64
import android.util.Log
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject
import sim2.app.talleb_5edma.util.BASE_URL
import sim2.app.talleb_5edma.util.forceClearAllData
import java.io.ByteArrayInputStream
import java.net.URISyntaxException
import kotlin.collections.get

class WebSocketCallManager(private val context: Context) {

    // Socket.IO connection
    private var socket: Socket? = null

    // Media Stream Manager
    var mediaStreamManager: MediaStreamManager? = null

    // Audio playback
    private var audioTrack: AudioTrack? = null
    private var mediaPlayer: MediaPlayer? = null

    // User information
    var userId: String = ""
    var userName: String = ""

    var toUserName: String = ""
    var toUserImage: String = ""

    // Server configuration
    //private val serverUrl = "http://192.168.1.34:3005"
    //private val serverUrl = "http://10.0.2.2:3005"
    //private val serverUrl = "https://talleb-5edma.onrender.com"
    private val serverUrl = BASE_URL

    // Performance monitoring
    private val performanceMonitor = PerformanceMonitor()

    // Network quality monitoring
    private val networkMonitor = NetworkQualityMonitor(context)
    private var currentNetworkQuality = NetworkQualityMonitor.NetworkQuality.GOOD
    private var adaptiveQualityEnabled = true

    // State Flows
    private val _callState = MutableStateFlow<CallState>(CallState.Idle)
    val callState: StateFlow<CallState> = _callState.asStateFlow()

    private val _callData = MutableStateFlow(CallData())
    val callData: StateFlow<CallData> = _callData.asStateFlow()

    private val _remoteVideoFrame = MutableStateFlow<String?>(null)
    val remoteVideoFrame: StateFlow<String?> = _remoteVideoFrame.asStateFlow()

    private val _connectionStatus = MutableStateFlow(false)
    val connectionStatus: StateFlow<Boolean> = _connectionStatus.asStateFlow()

    private val _isVideoStreaming = MutableStateFlow(false)
    val isVideoStreaming: StateFlow<Boolean> = _isVideoStreaming.asStateFlow()

    private val _isAudioStreaming = MutableStateFlow(false)
    val isAudioStreaming: StateFlow<Boolean> = _isAudioStreaming.asStateFlow()

    private val _callMessages = MutableStateFlow<List<CallMessage>>(emptyList())
    val callMessages: StateFlow<List<CallMessage>> = _callMessages.asStateFlow()

    // Network quality state
    private val _networkQuality = MutableStateFlow(NetworkQualityMonitor.NetworkQuality.GOOD)
    val networkQuality: StateFlow<NetworkQualityMonitor.NetworkQuality> = _networkQuality.asStateFlow()

    // Audio configuration
    private val audioSampleRate = 16000
    private val audioChannelConfig = AudioFormat.CHANNEL_OUT_MONO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT

    sealed class CallState {
        object Idle : CallState()
        object Connecting : CallState()
        data class OutgoingCall(val toUserId: String) : CallState()
        data class IncomingCall(
            val callId: String,
            val fromUserId: String,
            val fromUserName: String,
            val isVideoCall: Boolean
        ) : CallState()
        data class InCall(val roomId: String) : CallState()
        data class CallFailed(val reason: String) : CallState()
    }

    data class CallData(
        val callId: String = "",
        val chatId: String = "",
        val fromUserId: String = "",
        val fromUserName: String = "",
        val toUserId: String = "",
        val isVideoCall: Boolean = false,
        val timestamp: String = ""
    )

    data class CallMessage(
        val message: String,
        val userId: String,
        val userName: String,
        val timestamp: String,
        val isLocal: Boolean = false
    )

    // Performance monitoring class
    private class PerformanceMonitor {
        private var frameCount = 0
        private var lastLogTime = System.currentTimeMillis()

        fun logFrame() {
            frameCount++
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastLogTime > 5000) { // Log every 5 seconds
                Log.d("Performance", "Frames sent: $frameCount in 5s")
                frameCount = 0
                lastLogTime = currentTime
            }
        }
    }

    // Network Quality Monitor Class
    class NetworkQualityMonitor(private val context: Context) {

        enum class NetworkQuality {
            POOR,      // 2G/Edge, high latency
            FAIR,      // 3G, moderate latency
            GOOD,      // 4G/LTE
            EXCELLENT  // WiFi, 5G
        }

        interface NetworkQualityListener {
            fun onNetworkQualityChanged(quality: NetworkQuality)
            fun onNetworkTypeChanged(type: String)
        }

        private var listener: NetworkQualityListener? = null
        private val handler = Handler(Looper.getMainLooper())
        private var monitoring = false

        // Network metrics
        private var lastPacketLoss = 0.0
        private var lastLatency = 0L
        private var lastBandwidth = 0L

        fun startMonitoring(listener: NetworkQualityListener) {
            this.listener = listener
            monitoring = true
            handler.post(monitoringRunnable)
            Log.d("NetworkMonitor", "Started network quality monitoring")
        }

        fun stopMonitoring() {
            monitoring = false
            handler.removeCallbacks(monitoringRunnable)
            Log.d("NetworkMonitor", "Stopped network quality monitoring")
        }

        private val monitoringRunnable = object : Runnable {
            override fun run() {
                if (monitoring) {
                    checkNetworkQuality()
                    handler.postDelayed(this, 2000) // Check every 2 seconds
                }
            }
        }

        private fun checkNetworkQuality() {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val network = connectivityManager.activeNetwork
            val capabilities = connectivityManager.getNetworkCapabilities(network)

            val networkType = getNetworkType(capabilities)
            val quality = estimateNetworkQuality(capabilities, networkType)

            listener?.onNetworkTypeChanged(networkType)
            listener?.onNetworkQualityChanged(quality)

            Log.d("NetworkMonitor", "Network: $networkType, Quality: $quality")
        }

        private fun getNetworkType(capabilities: NetworkCapabilities?): String {
            return when {
                capabilities == null -> "DISCONNECTED"
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "WiFi"
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                    when {
                        capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) -> "MOBILE"
                        else -> "MOBILE_LIMITED"
                    }
                }
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "ETHERNET"
                else -> "UNKNOWN"
            }
        }

        private fun estimateNetworkQuality(capabilities: NetworkCapabilities?, networkType: String): NetworkQuality {
            return when (networkType) {
                "WiFi", "ETHERNET" -> {
                    // Check if it's actually good WiFi
                    if (capabilities?.linkDownstreamBandwidthKbps ?: 0 > 5000) {
                        NetworkQuality.EXCELLENT
                    } else {
                        NetworkQuality.GOOD
                    }
                }
                "MOBILE" -> {
                    val bandwidth = capabilities?.linkDownstreamBandwidthKbps ?: 0
                    when {
                        bandwidth > 10000 -> NetworkQuality.GOOD      // LTE Advanced
                        bandwidth > 2000 -> NetworkQuality.FAIR       // 4G/LTE
                        else -> NetworkQuality.POOR                   // 3G/Edge
                    }
                }
                "MOBILE_LIMITED" -> NetworkQuality.POOR
                else -> NetworkQuality.POOR
            }
        }

        // Call these methods from WebSocket events to update network metrics
        fun updatePacketLoss(lossPercentage: Double) {
            lastPacketLoss = lossPercentage
        }

        fun updateLatency(latencyMs: Long) {
            lastLatency = latencyMs
        }

        fun updateBandwidth(bandwidthKbps: Long) {
            lastBandwidth = bandwidthKbps
        }
    }

    enum class StreamQuality(
        val resolution: String,
        val width: Int,
        val height: Int,
        val quality: Int,
        val estimatedBitrate: Int // kbps
    ) {
        LOW("240p", 320, 240, 50, 300),
        MEDIUM("360p", 480, 360, 70, 600),
        HIGH("480p", 640, 480, 80, 1200)
    }

    // Initialize Socket.IO connection
    fun initializeSocket() {
        try {
            val options = IO.Options().apply {
                transports = arrayOf("websocket", "polling")
                forceNew = true
                reconnection = true
                reconnectionAttempts = 5
                reconnectionDelay = 1000
                timeout = 20000
            }

            socket = IO.socket(serverUrl, options).apply {
                // Connection events
                on(Socket.EVENT_CONNECT) {
                    Log.d("WebSocketCallManager", "✅ Connected to server")
                    _connectionStatus.value = true
                    registerUser()
                }

                on(Socket.EVENT_DISCONNECT) {
                    Log.d("WebSocketCallManager", "❌ Disconnected from server")
                    _connectionStatus.value = false
                }

                on(Socket.EVENT_CONNECT_ERROR) { args ->
                    Log.e("WebSocketCallManager", "Connection error: ${args.joinToString()}")
                    _connectionStatus.value = false
                }

                // Call events
                on("register-success") { args ->
                    Log.d("WebSocketCallManager", "✅ User registered successfully")
                }

                on("register-error") { args ->
                    Log.e("WebSocketCallManager", "Registration error: ${args.joinToString()}")
                }

                on("incoming-call") { args ->
                    try {
                        val data = args[0] as JSONObject
                        val callId = data.getString("callId")
                        val roomId = data.getString("roomId")
                        val fromUserId = data.getString("fromUserId")
                        val fromUserName = data.getString("fromUserName")
                        val isVideoCall = data.getBoolean("isVideoCall")
                        val timestamp = data.getString("timestamp")

                        Log.d("WebSocketCallManager", "📞 Incoming call from $fromUserName")

                        _callData.value = CallData(
                            callId = callId,
                            chatId = roomId,
                            fromUserId = fromUserId,
                            fromUserName = fromUserName,
                            isVideoCall = isVideoCall,
                            timestamp = timestamp
                        )

                        _callState.value = CallState.IncomingCall(
                            callId = callId,
                            fromUserId = fromUserId,
                            fromUserName = fromUserName,
                            isVideoCall = isVideoCall
                        )

                    } catch (e: Exception) {
                        Log.e("WebSocketCallManager", "Error processing incoming call", e)
                    }
                }

                on("call-started") { args ->
                    Log.d("WebSocketCallManager", "📞 Call started successfully")
                }

                on("call-request-failed") { args ->
                    val data = args[0] as JSONObject
                    val reason = data.getString("reason")
                    Log.e("WebSocketCallManager", "Call request failed: $reason")
                    _callState.value = CallState.CallFailed(reason)
                }

                on("call-response") { args ->
                    try {
                        val data = args[0] as JSONObject
                        val callId = data.getString("callId")
                        val accepted = data.getBoolean("accepted")

                        Log.d("WebSocketCallManager", "Call response: accepted=$accepted")

                        if (accepted) {
                            // Call was accepted, wait for join-call-room
                            Log.d("WebSocketCallManager", "✅ Call accepted, waiting to join room")
                        } else {
                            _callState.value = CallState.CallFailed("Call declined")
                        }
                    } catch (e: Exception) {
                        Log.e("WebSocketCallManager", "Error processing call response", e)
                    }
                }

                on("join-call-room") { args ->
                    try {
                        val data = args[0] as JSONObject
                        val roomId = data.getString("roomId")
                        val callId = data.getString("callId")

                        Log.d("WebSocketCallManager", "🎉 Joining call room: $roomId")

                        // Join the room
                        joinCallRoom(roomId)

                        // Update state
                        _callState.value = CallState.InCall(roomId)
                        _callData.value = _callData.value.copy(chatId = roomId, callId = callId)

                        // Initialize media streams
                        initializeMediaStream()
                        startMediaStreams()

                        // Start network monitoring when call begins
                        startNetworkMonitoring()

                        // Trigger navigation to call screen
                        handleCallStarted()

                    } catch (e: Exception) {
                        Log.e("WebSocketCallManager", "Error joining call room", e)
                        _callState.value = CallState.CallFailed("Failed to join call room")
                    }
                }

                on("call-cancelled") { args ->
                    Log.d("WebSocketCallManager", "📞 Call cancelled by caller")
                    _callState.value = CallState.CallFailed("Call cancelled")
                }

                on("call-timeout") { args ->
                    Log.d("WebSocketCallManager", "⏰ Call timed out")
                    _callState.value = CallState.CallFailed("Call timed out")
                }

                // In WebSocketCallManager initialization, add:
                on("call-ended") { args ->
                    try {
                        val data = args[0] as JSONObject
                        val reason = data.getString("reason")

                        Log.d("WebSocketCallManager", "📞 Call ended: $reason")

                        // Clean up media streams
                        cleanupMediaStreams()
                        networkMonitor.stopMonitoring()

                        // Update state
                        _callState.value = CallState.Idle
                        _callData.value = CallData()
                        _remoteVideoFrame.value = null

                        // Navigate back
                        onNavigateBackFromCall?.invoke()

                    } catch (e: Exception) {
                        Log.e("WebSocketCallManager", "Error processing call-ended", e)
                    }
                }
                // Media events
                on("media-frame") { args ->
                    try {
                        val data = args[0] as JSONObject
                        val type = data.getString("type")
                        val frameData = data.optString("frameData", null)
                        val audioData = data.optString("audioData", null)
                        val userId = data.getString("userId")
                        val userName = data.getString("userName")

                        when (type) {
                            "video" -> {
                                frameData?.let {
                                    _remoteVideoFrame.value = it
                                }
                            }
                            "audio" -> {
                                audioData?.let {
                                    playAudioData(it)
                                }
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("WebSocketCallManager", "Error processing media frame", e)
                    }
                }

                on("call-message") { args ->
                    try {
                        val data = args[0] as JSONObject
                        val message = data.getString("message")
                        val userId = data.getString("userId")
                        val userName = data.getString("userName")
                        val timestamp = data.getString("timestamp")

                        val callMessage = CallMessage(
                            message = message,
                            userId = userId,
                            userName = userName,
                            timestamp = timestamp,
                            isLocal = false
                        )

                        _callMessages.value = _callMessages.value + callMessage

                    } catch (e: Exception) {
                        Log.e("WebSocketCallManager", "Error processing call message", e)
                    }
                }

                // Network metrics events
                on("network-metrics") { args ->
                    try {
                        val data = args[0] as JSONObject
                        val packetLoss = data.optDouble("packetLoss", 0.0)
                        val latency = data.optLong("latency", 0L)
                        val bandwidth = data.optLong("bandwidth", 0L)

                        networkMonitor.updatePacketLoss(packetLoss)
                        networkMonitor.updateLatency(latency)
                        networkMonitor.updateBandwidth(bandwidth)

                        Log.d("NetworkMetrics", "Packet loss: $packetLoss%, Latency: ${latency}ms, Bandwidth: ${bandwidth}kbps")

                    } catch (e: Exception) {
                        Log.e("WebSocketCallManager", "Error processing network metrics", e)
                    }
                }

                // Debug events
                on("debug-media-info") { args ->
                    try {
                        val data = args[0] as JSONObject
                        Log.d("WebSocketCallManager", "🔍 Debug media info: $data")
                    } catch (e: Exception) {
                        Log.e("WebSocketCallManager", "Error processing debug info", e)
                    }
                }

                on("connection-status") { args ->
                    try {
                        val data = args[0] as JSONObject
                        Log.d("WebSocketCallManager", "Connection status: $data")
                    } catch (e: Exception) {
                        Log.e("WebSocketCallManager", "Error processing connection status", e)
                    }
                }
            }

            socket?.connect()
            Log.d("WebSocketCallManager", "🔄 Attempting to connect to $serverUrl")

        } catch (e: URISyntaxException) {
            Log.e("WebSocketCallManager", "Invalid server URL", e)
            _callState.value = CallState.CallFailed("Invalid server configuration")
        } catch (e: Exception) {
            Log.e("WebSocketCallManager", "Socket initialization failed", e)
            _callState.value = CallState.CallFailed("Connection failed: ${e.message}")
        }
    }

    // Network Quality Monitoring
    fun startNetworkMonitoring() {
        networkMonitor.startMonitoring(object : NetworkQualityMonitor.NetworkQualityListener {
            override fun onNetworkQualityChanged(quality: NetworkQualityMonitor.NetworkQuality) {
                Log.d("WebSocketCallManager", "Network quality changed: $quality")
                currentNetworkQuality = quality
                _networkQuality.value = quality

                if (adaptiveQualityEnabled) {
                    adjustQualityBasedOnNetwork(quality)
                }
            }

            override fun onNetworkTypeChanged(type: String) {
                Log.d("WebSocketCallManager", "Network type: $type")
                // You can use this for UI updates or additional logic
            }
        })
    }

    private fun adjustQualityBasedOnNetwork(quality: NetworkQualityMonitor.NetworkQuality) {
        when (quality) {
            NetworkQualityMonitor.NetworkQuality.EXCELLENT -> {
                // High quality - WiFi/5G
                adjustStreamingQuality(StreamQuality.HIGH)
                mediaStreamManager?.setStreamingQuality(640, 480, 80) // Original quality
                Log.d("WebSocketCallManager", "🟢 Excellent network - Using HIGH quality")
            }
            NetworkQualityMonitor.NetworkQuality.GOOD -> {
                // Good quality - 4G/LTE
                adjustStreamingQuality(StreamQuality.MEDIUM)
                mediaStreamManager?.setStreamingQuality(480, 360, 70)
                Log.d("WebSocketCallManager", "🔵 Good network - Using MEDIUM quality")
            }
            NetworkQualityMonitor.NetworkQuality.FAIR -> {
                // Fair quality - 3G
                adjustStreamingQuality(StreamQuality.LOW)
                mediaStreamManager?.setStreamingQuality(320, 240, 50)
                Log.d("WebSocketCallManager", "🟡 Fair network - Using LOW quality")
            }
            NetworkQualityMonitor.NetworkQuality.POOR -> {
                // Poor quality - 2G/Edge
                adjustStreamingQuality(StreamQuality.LOW)
                mediaStreamManager?.setStreamingQuality(240, 180, 30)
                // Consider audio-only mode for very poor networks
                if (_isVideoStreaming.value && _callData.value.isVideoCall) {
                    toggleVideo() // Switch to audio only
                    sendCallMessage("📶 Network poor - switched to audio only")
                }
                Log.d("WebSocketCallManager", "🔴 Poor network - Using MINIMAL quality")
            }
        }
    }

    fun setAdaptiveQualityEnabled(enabled: Boolean) {
        adaptiveQualityEnabled = enabled
        if (enabled) {
            adjustQualityBasedOnNetwork(currentNetworkQuality)
        }
        Log.d("WebSocketCallManager", "Adaptive quality: $enabled")
    }

    // User registration
    private fun registerUser() {
        try {
            socket?.emit("register", JSONObject().apply {
                put("userId", userId)
                put("userName", userName)
            })
            Log.d("WebSocketCallManager", "📝 Registering user: $userId")
        } catch (e: Exception) {
            Log.e("WebSocketCallManager", "Error registering user", e)
        }
    }

    fun handleCallAccepted() {
        try {
            val currentCallData = _callData.value
            if (currentCallData.callId.isNotEmpty()) {
                socket?.emit("call-response", JSONObject().apply {
                    put("callId", currentCallData.callId)
                    put("accepted", true)
                })
                Log.d("WebSocketCallManager", "✅ Call accepted, waiting for room join")

                // Set a temporary state while waiting for room join
                _callState.value = CallState.Connecting
            }
        } catch (e: Exception) {
            Log.e("WebSocketCallManager", "Error accepting call", e)
            _callState.value = CallState.CallFailed("Error accepting call")
        }
    }

    fun acceptCall() {
        handleCallAccepted()
    }

    fun rejectCall() {
        try {
            val currentCallData = _callData.value
            if (currentCallData.callId.isNotEmpty()) {
                socket?.emit("call-response", JSONObject().apply {
                    put("callId", currentCallData.callId)
                    put("accepted", false)
                })
                Log.d("WebSocketCallManager", "❌ Rejecting call: ${currentCallData.callId}")
            }
            _callState.value = CallState.Idle
        } catch (e: Exception) {
            Log.e("WebSocketCallManager", "Error rejecting call", e)
        }
    }

    fun cancelCall() {
        try {
            val currentCallData = _callData.value
            if (currentCallData.callId.isNotEmpty()) {
                socket?.emit("cancel-call", JSONObject().apply {
                    put("callId", currentCallData.callId)
                })
                Log.d("WebSocketCallManager", "📞 Cancelling call: ${currentCallData.callId}")
            }
            _callState.value = CallState.Idle
        } catch (e: Exception) {
            Log.e("WebSocketCallManager", "Error cancelling call", e)
        }
    }

    fun hangUp() {
        try {
            stopMediaStreams()
            cleanupMediaStreams()
            cleanupResources()
            networkMonitor.stopMonitoring()

            val currentCallData = _callData.value
            if (currentCallData.chatId.isNotEmpty()) {
                socket?.emit("leave-call", JSONObject().apply {
                    put("roomId", currentCallData.chatId)
                })
            }

            _callState.value = CallState.Idle
            _callData.value = CallData()
            _remoteVideoFrame.value = null
            _callMessages.value = emptyList()

            Log.d("WebSocketCallManager", "📞 Hang up complete")

        } catch (e: Exception) {
            Log.e("WebSocketCallManager", "Error during hang up", e)
        }
    }

    // Room management
    private fun joinCallRoom(roomId: String) {
        try {
            socket?.emit("join-call", JSONObject().apply {
                put("roomId", roomId)
                put("userId", userId)
                put("userName", userName)
            })
            Log.d("WebSocketCallManager", "🚪 Joining room: $roomId")
        } catch (e: Exception) {
            Log.e("WebSocketCallManager", "Error joining room", e)
        }
    }

    // Media Stream Management
    fun initializeMediaStream() {
        mediaStreamManager = MediaStreamManager(context).apply {
            onVideoFrame = { frameData ->
                sendVideoFrame(frameData)
            }
            onAudioData = { audioData ->
                sendAudioData(audioData)
            }
            onError = { error ->
                Log.e("WebSocketCallManager", "MediaStream error: $error")
                _callState.value = CallState.CallFailed("Media error: $error")
            }
            onCameraStateChanged = { state ->
                Log.d("WebSocketCallManager", "Media state: $state")
                _isVideoStreaming.value = isStreaming()
                _isAudioStreaming.value = isAudioStreaming()
            }
            onSpeakingStateChanged = { isSpeaking ->
                Log.d("WebSocketCallManager", "Speaking state: $isSpeaking")
            }
        }
        Log.d("WebSocketCallManager", "🎥 MediaStreamManager initialized")
    }

    fun startMediaStreams() {
        if (_callData.value.isVideoCall) {
            mediaStreamManager?.startVideoStream()
            _isVideoStreaming.value = true
        }
        mediaStreamManager?.startAudioStream()
        _isAudioStreaming.value = true
        Log.d("WebSocketCallManager", "🎬 Media streams started")
    }

    fun stopMediaStreams() {
        mediaStreamManager?.stopVideoStream()
        mediaStreamManager?.stopAudioStream()
        _isVideoStreaming.value = false
        _isAudioStreaming.value = false
        Log.d("WebSocketCallManager", "⏹️ Media streams stopped")
    }

    fun cleanupMediaStreams() {
        mediaStreamManager?.cleanup()
        mediaStreamManager = null
        _isVideoStreaming.value = false
        _isAudioStreaming.value = false
        Log.d("WebSocketCallManager", "🧹 Media streams cleaned up")
    }

    fun switchCamera() {
        mediaStreamManager?.switchCamera()
        Log.d("WebSocketCallManager", "🔄 Switching camera")
    }

    fun toggleVideo() {
        if (_isVideoStreaming.value) {
            mediaStreamManager?.stopVideoStream()
            _isVideoStreaming.value = false
        } else {
            mediaStreamManager?.startVideoStream()
            _isVideoStreaming.value = true
        }
        Log.d("WebSocketCallManager", "📹 Video toggled: ${_isVideoStreaming.value}")
    }

    fun toggleAudio() {
        if (_isAudioStreaming.value) {
            mediaStreamManager?.stopAudioStream()
            _isAudioStreaming.value = false
        } else {
            mediaStreamManager?.startAudioStream()
            _isAudioStreaming.value = true
        }
        Log.d("WebSocketCallManager", "🎤 Audio toggled: ${_isAudioStreaming.value}")
    }

    // Media sending methods
    private fun sendVideoFrame(frameData: String) {
        try {
            performanceMonitor.logFrame()

            if (_callState.value is CallState.InCall) {
                socket?.emit("media-frame", JSONObject().apply {
                    put("roomId", _callData.value.chatId)
                    put("type", "video")
                    put("frameData", frameData)
                    put("userId", userId)
                    put("userName", userName)
                    put("timestamp", System.currentTimeMillis())
                })
            }
        } catch (e: Exception) {
            Log.e("WebSocketCallManager", "Error sending video frame", e)
        }
    }

    private fun sendAudioData(audioData: String) {
        try {
            if (_callState.value is CallState.InCall) {
                socket?.emit("media-frame", JSONObject().apply {
                    put("roomId", _callData.value.chatId)
                    put("type", "audio")
                    put("audioData", audioData)
                    put("userId", userId)
                    put("userName", userName)
                    put("timestamp", System.currentTimeMillis())
                })
            }
        } catch (e: Exception) {
            Log.e("WebSocketCallManager", "Error sending audio data", e)
        }
    }

    // Audio playback
    private fun playAudioData(audioData: String) {
        try {
            val audioBytes = Base64.decode(audioData, Base64.DEFAULT)

            if (audioTrack == null) {
                val bufferSize = AudioTrack.getMinBufferSize(audioSampleRate, audioChannelConfig, audioFormat)
                audioTrack = AudioTrack(
                    android.media.AudioManager.STREAM_VOICE_CALL,
                    audioSampleRate,
                    audioChannelConfig,
                    audioFormat,
                    bufferSize * 10, // Use larger buffer
                    AudioTrack.MODE_STREAM
                )
                audioTrack?.play()
                Log.d("WebSocketCallManager", "✅ AudioTrack initialized with buffer size: $bufferSize")
            }

            // Write directly as bytes (AudioTrack will handle the conversion)
            val bytesWritten = audioTrack?.write(audioBytes, 0, audioBytes.size, AudioTrack.WRITE_BLOCKING) ?: 0

            if (bytesWritten != audioBytes.size) {
                Log.w("WebSocketCallManager", "Audio underrun: wrote $bytesWritten/${audioBytes.size} bytes")
            }

        } catch (e: Exception) {
            Log.e("WebSocketCallManager", "Error playing audio", e)
            // Reset audio track on error
            audioTrack?.stop()
            audioTrack?.release()
            audioTrack = null
        }
    }

    // Message sending
    fun sendCallMessage(message: String) {
        try {
            if (_callState.value is CallState.InCall) {
                socket?.emit("call-message", JSONObject().apply {
                    put("roomId", _callData.value.chatId)
                    put("message", message)
                    put("userId", userId)
                    put("userName", userName)
                })

                // Add to local messages
                val callMessage = CallMessage(
                    message = message,
                    userId = userId,
                    userName = userName,
                    timestamp = System.currentTimeMillis().toString(),
                    isLocal = true
                )

                _callMessages.value = _callMessages.value + callMessage

                Log.d("WebSocketCallManager", "💬 Message sent: $message")
            }
        } catch (e: Exception) {
            Log.e("WebSocketCallManager", "Error sending message", e)
        }
    }

    // Quality control methods
    fun adjustStreamingQuality(quality: StreamQuality) {
        when (quality) {
            StreamQuality.LOW -> {
                Log.d("WebSocketCallManager", "Setting streaming quality: LOW (${quality.estimatedBitrate}kbps)")
                mediaStreamManager?.setStreamingQuality(quality.width, quality.height, quality.quality)
            }
            StreamQuality.MEDIUM -> {
                Log.d("WebSocketCallManager", "Setting streaming quality: MEDIUM (${quality.estimatedBitrate}kbps)")
                mediaStreamManager?.setStreamingQuality(quality.width, quality.height, quality.quality)
            }
            StreamQuality.HIGH -> {
                Log.d("WebSocketCallManager", "Setting streaming quality: HIGH (${quality.estimatedBitrate}kbps)")
                mediaStreamManager?.setStreamingQuality(quality.width, quality.height, quality.quality)
            }
        }
    }

    // Debug and utility methods
    fun debugMediaStreaming() {
        try {
            socket?.emit("debug-media-streaming", JSONObject().apply {
                put("roomId", _callData.value.chatId)
                put("userId", userId)
                put("action", "debug")
            })
            Log.d("WebSocketCallManager", "🔍 Debug media streaming")
        } catch (e: Exception) {
            Log.e("WebSocketCallManager", "Error debugging media", e)
        }
    }

    fun testAudioPlayback() {
        try {
            // Test audio playback with a simple tone
            val testMessage = CallMessage(
                message = "🔊 Audio test message",
                userId = "system",
                userName = "System",
                timestamp = System.currentTimeMillis().toString(),
                isLocal = true
            )
            _callMessages.value = _callMessages.value + testMessage
            Log.d("WebSocketCallManager", "🎵 Audio playback test")
        } catch (e: Exception) {
            Log.e("WebSocketCallManager", "Error testing audio", e)
        }
    }

    fun setAudioVolume(volume: Float) {
        audioTrack?.setVolume(volume)
        Log.d("WebSocketCallManager", "🔊 Volume set to: $volume")
    }

    fun getConnectionStatus(): Boolean {
        return _connectionStatus.value
    }

    // Cleanup
    fun disconnect() {
        try {
            cleanupMediaStreams()
            cleanupResources()
            networkMonitor.stopMonitoring()
            audioTrack?.stop()
            audioTrack?.release()
            audioTrack = null

            socket?.disconnect()
            socket = null

            _connectionStatus.value = false
            _callState.value = CallState.Idle
            _callData.value = CallData()
            _networkQuality.value = NetworkQualityMonitor.NetworkQuality.GOOD

            Log.d("WebSocketCallManager", "🔌 Disconnected and cleaned up")
        } catch (e: Exception) {
            Log.e("WebSocketCallManager", "Error during disconnect", e)
        }
    }

    // Memory management
    fun cleanupResources() {
        // Clear large data
        _remoteVideoFrame.value = null
        _callMessages.value = emptyList()

        // Force GC (use sparingly)
        System.gc()
    }

    // Utility methods for UI
    fun getRemoteVideoBitmap(): Bitmap? {
        return _remoteVideoFrame.value?.let { frameData ->
            try {
                val imageBytes = Base64.decode(frameData, Base64.DEFAULT)
                BitmapFactory.decodeStream(ByteArrayInputStream(imageBytes))
            } catch (e: Exception) {
                null
            }
        }
    }

    fun isInCall(): Boolean {
        return _callState.value is CallState.InCall
    }

    fun hasActiveCall(): Boolean {
        return _callState.value !is CallState.Idle
    }

    // User management
    fun setUserInfo(userId: String, userName: String) {
        this.userId = userId
        this.userName = userName
        // Re-register with new user info if already connected
        if (socket?.connected() == true) {
            registerUser()
        }
    }

    // Call management methods that were referenced in TestCallApp
    fun startCallFromChat(toUserId: String, isVideoCall: Boolean, chatId: String) {
        makeCall(toUserId, isVideoCall,chatId)
    }

    fun retryCall() {
        // For simplicity, we'll just clear the failed state
        if (_callState.value is CallState.CallFailed) {
            _callState.value = CallState.Idle
        }
    }

    fun getConnectedUsers() {
        // This would typically emit a socket event to get connected users
        // For now, we'll just log it
        Log.d("WebSocketCallManager", "Getting connected users...")
        socket?.emit("get-connected-users")
    }

    fun cancelWaiting() {
        if (_callState.value is CallState.OutgoingCall) {
            cancelCall()
        }
    }

    // Event callbacks
    var onNavigateToCallScreen: (() -> Unit)? = null
    var onNavigateBackFromCall: (() -> Unit)? = null
    var onEvent: ((String) -> Unit)? = null

    // Add this method to handle navigation when call starts
    private fun handleCallStarted() {
        onNavigateToCallScreen?.invoke()
    }

    // Update the makeCall method to trigger navigation
    fun makeCall(toUserId: String, isVideoCall: Boolean = true , chatId : String) {
        try {
            val roomId = "room_${chatId}"

            _callData.value = CallData(
                chatId = roomId,
                fromUserId = userId,
                fromUserName = userName,
                toUserId = toUserId,
                isVideoCall = isVideoCall,
                timestamp = System.currentTimeMillis().toString()
            )

            _callState.value = CallState.OutgoingCall(toUserId)

            socket?.emit("call-request", JSONObject().apply {
                put("roomId", roomId)
                put("fromUserId", userId)
                put("fromUserName", userName)
                put("toUserId", toUserId)
                put("isVideoCall", isVideoCall)
            })

            Log.d("WebSocketCallManager", "📞 Making call to $toUserId (Video: $isVideoCall)")

        } catch (e: Exception) {
            Log.e("WebSocketCallManager", "Error making call", e)
            _callState.value = CallState.CallFailed("Failed to make call: ${e.message}")
        }
    }

    // Get current network quality for UI display
    fun getCurrentNetworkQuality(): NetworkQualityMonitor.NetworkQuality {
        return currentNetworkQuality
    }

    // Check if adaptive quality is enabled
    fun isAdaptiveQualityEnabled(): Boolean {
        return adaptiveQualityEnabled
    }

    // Add this to your WebSocketCallManager class
    fun resetCallState() {
        // Reset any persistent error states or flags
        _callState.value = CallState.Idle
        _callData.value = CallData() // Reset to default
    }
}