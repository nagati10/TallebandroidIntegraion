package sim2.app.talleb_5edma.network

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

/**
 * AI Chat API for interview training
 * Backend: https://voice-chatbot-k3fe.onrender.com
 */

data class AiTextChatRequest(
    val text: String,
    val session_id: String,
    val mode: String = "coaching", // "coaching" or "employer_interview"
    val user_details: Map<String, Any>,
    val offer_details: Map<String, Any>,
    val chat_history: List<List<String>> = emptyList()
)

data class AiChatResponse(
    val success: Boolean,
    val ai_response: String?,
    val transcribed_text: String?,
    val audio_response: String?, // Base64 encoded audio (MP3)
    val language: String?,
    val session_id: String?,
    val mode: String?,
    val error: String?
)

interface AiChatApi {
    
    /**
     * Send text message to AI coach
     */
    @POST("/api/text-chat")
    suspend fun textChat(@Body request: AiTextChatRequest): AiChatResponse
    
    /**
     * Send voice message to AI coach
     * Returns transcribed text + AI response with audio
     */
    @Multipart
    @POST("/api/voice-chat")
    suspend fun voiceChat(
        @Part audio: MultipartBody.Part,
        @Part("session_id") sessionId: RequestBody,
        @Part("mode") mode: RequestBody,
        @Part("user_details") userDetails: RequestBody,
        @Part("offer_details") offerDetails: RequestBody
    ): AiChatResponse
}
