package sim2.app.talleb_5edma.network

import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * Repository for AI Interview Training Chat
 * Handles communication with voice chatbot backend
 */
class AiChatRepository {
    
    private val baseUrl = "https://voice-chatbot-k3fe.onrender.com"
    
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()
    
    private val retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    
    private val aiChatApi = retrofit.create(AiChatApi::class.java)
    
    /**
     * Send text message to AI coach
     */
    suspend fun sendTextMessage(
        text: String,
        sessionId: String,
        mode: String = "coaching",
        userDetails: Map<String, Any>,
        offerDetails: Map<String, Any>,
        chatHistory: List<List<String>> = emptyList()
    ): AiChatResponse {
        println("Ai_catlog: AiChatRepository.sendTextMessage - Text: $text")
        println("Ai_catlog: AiChatRepository.sendTextMessage - SessionId: $sessionId")
        println("Ai_catlog: AiChatRepository.sendTextMessage - Mode: $mode")
        
        return try {
            val request = AiTextChatRequest(
                text = text,
                session_id = sessionId,
                mode = mode,
                user_details = userDetails,
                offer_details = offerDetails,
                chat_history = chatHistory
            )
            
            val response = aiChatApi.textChat(request)
            
            println("Ai_catlog: AiChatRepository.sendTextMessage - Response success: ${response.success}")
            println("Ai_catlog: AiChatRepository.sendTextMessage - AI response: ${response.ai_response?.take(100)}")
            
            response
        } catch (e: Exception) {
            println("Ai_catlog: AiChatRepository.sendTextMessage - Error: ${e.message}")
            e.printStackTrace()
            
            AiChatResponse(
                success = false,
                ai_response = null,
                transcribed_text = null,
                audio_response = null,
                language = null,
                session_id = sessionId,
                mode = mode,
                error = e.message ?: "Unknown error"
            )
        }
    }
    
    /**
     * Send voice message to AI coach
     * Audio file should be in WebM or MP3 format
     */
    suspend fun sendVoiceMessage(
        audioFile: File,
        sessionId: String,
        mode: String = "coaching",
        userDetails: Map<String, Any>,
        offerDetails: Map<String, Any>
    ): AiChatResponse {
        println("Ai_catlog: AiChatRepository.sendVoiceMessage - SessionId: $sessionId")
        println("Ai_catlog: AiChatRepository.sendVoiceMessage - Audio file size: ${audioFile.length()} bytes")
        println("Ai_catlog: AiChatRepository.sendVoiceMessage - Mode: $mode")
        
        return try {
            // Create multipart request
            val audioRequestBody = audioFile.asRequestBody("audio/webm".toMediaTypeOrNull())
            val audioPart = MultipartBody.Part.createFormData("audio", audioFile.name, audioRequestBody)
            
            val sessionIdBody = sessionId.toRequestBody("text/plain".toMediaTypeOrNull())
            val modeBody = mode.toRequestBody("text/plain".toMediaTypeOrNull())
            
            // Convert maps to JSON strings
            val gson = Gson()
            val userDetailsJson = gson.toJson(userDetails)
            val offerDetailsJson = gson.toJson(offerDetails)
            
            val userDetailsBody = userDetailsJson.toRequestBody("application/json".toMediaTypeOrNull())
            val offerDetailsBody = offerDetailsJson.toRequestBody("application/json".toMediaTypeOrNull())
            
            println("Ai_catlog: AiChatRepository.sendVoiceMessage - Sending request to backend...")
            
            val response = aiChatApi.voiceChat(
                audio = audioPart,
                sessionId = sessionIdBody,
                mode = modeBody,
                userDetails = userDetailsBody,
                offerDetails = offerDetailsBody
            )
            
            println("Ai_catlog: AiChatRepository.sendVoiceMessage - Response success: ${response.success}")
            println("Ai_catlog: AiChatRepository.sendVoiceMessage - Transcribed: ${response.transcribed_text}")
            println("Ai_catlog: AiChatRepository.sendVoiceMessage - AI response: ${response.ai_response?.take(100)}")
            println("Ai_catlog: AiChatRepository.sendVoiceMessage - Audio response length: ${response.audio_response?.length ?: 0}")
            
            response
        } catch (e: Exception) {
            println("Ai_catlog: AiChatRepository.sendVoiceMessage - Error: ${e.message}")
            if (e is retrofit2.HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                println("Ai_catlog: AiChatRepository.sendVoiceMessage - Error Body: $errorBody")
            }
            e.printStackTrace()
            
            AiChatResponse(
                success = false,
                ai_response = null,
                transcribed_text = null,
                audio_response = null,
                language = null,
                session_id = sessionId,
                mode = mode,
                error = e.message ?: "Unknown error"
            )
        }
    }
}
