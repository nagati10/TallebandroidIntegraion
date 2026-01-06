package sim2.app.talleb_5edma.network

import android.content.Context
import android.util.Log
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject
import sim2.app.talleb_5edma.models.InterviewInvitation

/**
 * WebSocket manager for real-time interview invitation notifications.
 * Connects to the AI service backend (voice-chatbot) for invitation events.
 */
class InterviewWebSocketManager(private val context: Context) {

    companion object {
        private const val TAG = "InterviewWebSocket"
        private const val SERVER_URL = "https://voice-chatbot-k3fe.onrender.com"
    }

    // Socket.IO connection
    private var socket: Socket? = null

    // User information
    private var userId: String = ""

    // Connection status
    private val _connectionStatus = MutableStateFlow(false)
    val connectionStatus: StateFlow<Boolean> = _connectionStatus.asStateFlow()

    // Invitation events - emits when a new invitation is received
    private val _pendingInvitation = MutableStateFlow<InterviewInvitation?>(null)
    val pendingInvitation: StateFlow<InterviewInvitation?> = _pendingInvitation.asStateFlow()

    /**
     * Connect to the WebSocket server and register the user
     */
    fun connect(userId: String) {
        if (userId.isBlank()) {
            Log.e(TAG, "❌ Cannot connect: userId is blank")
            return
        }

        this.userId = userId

        try {
            val options = IO.Options().apply {
                transports = arrayOf("websocket", "polling")
                forceNew = true
                reconnection = true
                reconnectionAttempts = 10
                reconnectionDelay = 1000
                timeout = 20000
            }

            socket = IO.socket(SERVER_URL, options).apply {
                // Connection events
                on(Socket.EVENT_CONNECT) {
                    Log.d(TAG, "✅ Connected to AI WebSocket server")
                    _connectionStatus.value = true
                    joinUserRoom()
                }

                on(Socket.EVENT_DISCONNECT) {
                    Log.d(TAG, "❌ Disconnected from AI WebSocket server")
                    _connectionStatus.value = false
                }

                on(Socket.EVENT_CONNECT_ERROR) { args ->
                    Log.e(TAG, "Connection error: ${args.joinToString()}")
                    _connectionStatus.value = false
                }

                // Custom events
                on("connect_response") { args ->
                    Log.d(TAG, "📥 Server acknowledged connection")
                }

                on("join_response") { args ->
                    try {
                        val data = args[0] as JSONObject
                        val success = data.getBoolean("success")
                        val message = data.getString("message")
                        Log.d(TAG, "📥 Join response: $success - $message")
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing join_response", e)
                    }
                }

                // ✨ INVITATION RECEIVED EVENT ✨
                on("invitation_received") { args ->
                    try {
                        val data = args[0] as JSONObject
                        Log.d(TAG, "🎉 INVITATION RECEIVED: $data")

                        val invitation = InterviewInvitation(
                            invitationId = data.getInt("invitation_id"),
                            chatId = data.getString("chat_id"),
                            fromUserId = data.getString("from_user_id"),
                            toUserId = userId, // The current user is the recipient
                            fromUserName = data.optString("from_user_name", "Company"),
                            offerId = data.optString("offer_id", null),
                            createdAt = data.optString("created_at", "")
                        )

                        _pendingInvitation.value = invitation
                        Log.d(TAG, "📬 Invitation set: ${invitation.fromUserName}")

                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing invitation_received", e)
                    }
                }

                // Pong for heartbeat
                on("pong") {
                    Log.d(TAG, "💓 Heartbeat received")
                }

                on("error") { args ->
                    Log.e(TAG, "❌ Server error: ${args.joinToString()}")
                }
            }

            socket?.connect()
            Log.d(TAG, "🔄 Connecting to $SERVER_URL for user $userId")

        } catch (e: Exception) {
            Log.e(TAG, "Socket initialization failed", e)
            _connectionStatus.value = false
        }
    }

    /**
     * Join the user's personal room to receive notifications
     */
    private fun joinUserRoom() {
        try {
            socket?.emit("join", JSONObject().apply {
                put("userId", userId)
            })
            Log.d(TAG, "📝 Joining room for user: $userId")
        } catch (e: Exception) {
            Log.e(TAG, "Error joining room", e)
        }
    }

    /**
     * Send a ping to keep the connection alive
     */
    fun sendPing() {
        try {
            socket?.emit("ping")
        } catch (e: Exception) {
            Log.e(TAG, "Error sending ping", e)
        }
    }

    /**
     * Clear the current pending invitation after it's been handled
     */
    fun clearPendingInvitation() {
        _pendingInvitation.value = null
    }

    /**
     * Disconnect from the WebSocket server
     */
    fun disconnect() {
        try {
            if (userId.isNotBlank()) {
                socket?.emit("leave", JSONObject().apply {
                    put("userId", userId)
                })
            }
            socket?.disconnect()
            socket?.off()
            socket = null
            _connectionStatus.value = false
            _pendingInvitation.value = null
            Log.d(TAG, "🔌 Disconnected and cleaned up")
        } catch (e: Exception) {
            Log.e(TAG, "Error during disconnect", e)
        }
    }

    /**
     * Check if connected
     */
    fun isConnected(): Boolean = socket?.connected() == true
}
