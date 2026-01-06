package sim2.app.talleb_5edma.network

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import sim2.app.talleb_5edma.models.*
import java.util.concurrent.TimeUnit

/**
 * Retrofit API interface for Interview Invitation endpoints
 */
interface InterviewInvitationApi {
    
    @POST("/api/send-interview-invitation")
    suspend fun sendInvitation(
        @Body request: SendInterviewInvitationRequest
    ): SendInterviewInvitationResponse
    
    @GET("/api/get-pending-invitations/{user_id}")
    suspend fun getPendingInvitations(
        @Path("user_id") userId: String
    ): GetPendingInvitationsResponse
    
    @POST("/api/accept-interview-invitation")
    suspend fun acceptInvitation(
        @Body request: RespondToInvitationRequest
    ): AcceptInvitationResponse
    
    @POST("/api/reject-interview-invitation")
    suspend fun rejectInvitation(
        @Body request: RespondToInvitationRequest
    ): RejectInvitationResponse
}

/**
 * Repository for managing interview invitations
 */
class InterviewInvitationRepository {
    
    private val baseUrl = "https://voice-chatbot-k3fe.onrender.com"
    
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    
    private val retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    
    private val api = retrofit.create(InterviewInvitationApi::class.java)
    
    /**
     * Send an interview invitation from enterprise to student
     */
    suspend fun sendInvitation(
        chatId: String,
        fromUserId: String,
        toUserId: String,
        fromUserName: String,
        offerId: String? = null
    ): SendInterviewInvitationResponse {
        return try {
            val request = SendInterviewInvitationRequest(
                chatId = chatId,
                fromUserId = fromUserId,
                toUserId = toUserId,
                fromUserName = fromUserName,
                offerId = offerId
            )
            println("🎯 Sending interview invitation: $fromUserName → Student (Chat: $chatId)")
            println("🎯 Request Payload: $request")
            val response = api.sendInvitation(request)
            println("✅ Invitation sent successfully: $response")
            return response
        } catch (e: Exception) {
            println("❌ Error sending invitation: ${e.message}")
            e.printStackTrace()
            if (e is retrofit2.HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                println("❌ Error Body: $errorBody")
            }
            SendInterviewInvitationResponse(
                success = false,
                error = "Failed to send invitation: ${e.message}"
            )
        }
    }
    
    /**
     * Get all pending interview invitations for a user
     */
    suspend fun getPendingInvitations(userId: String): GetPendingInvitationsResponse {
        return try {
            println("📬 Getting pending invitations for user: $userId")
            api.getPendingInvitations(userId)
        } catch (e: Exception) {
            println("❌ Error getting invitations: ${e.message}")
            GetPendingInvitationsResponse(
                success = false,
                error = "Failed to get invitations: ${e.message}"
            )
        }
    }
    
    /**
     * Accept an interview invitation
     */
    suspend fun acceptInvitation(invitationId: Int): AcceptInvitationResponse {
        return try {
            val request = RespondToInvitationRequest(invitationId = invitationId)
            println("✅ Accepting interview invitation: $invitationId")
            api.acceptInvitation(request)
        } catch (e: Exception) {
            println("❌ Error accepting invitation: ${e.message}")
            AcceptInvitationResponse(
                success = false,
                error = "Failed to accept invitation: ${e.message}"
            )
        }
    }
    
    /**
     * Reject an interview invitation
     */
    suspend fun rejectInvitation(invitationId: Int): RejectInvitationResponse {
        return try {
            val request = RespondToInvitationRequest(invitationId = invitationId)
            println("❌ Rejecting interview invitation: $invitationId")
            api.rejectInvitation(request)
        } catch (e: Exception) {
            println("❌ Error rejecting invitation: ${e.message}")
            RejectInvitationResponse(
                success = false,
                error = "Failed to reject invitation: ${e.message}"
            )
        }
    }
}
