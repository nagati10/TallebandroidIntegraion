package sim2.app.talleb_5edma.network

import com.google.gson.annotations.SerializedName

// Request for interview analysis
data class AnalyzeInterviewRequest(
    @SerializedName("session_id") val sessionId: String,
    @SerializedName("chat_id") val chatId: String,
    @SerializedName("user_details") val userDetails: Map<String, Any>,
    @SerializedName("offer_details") val offerDetails: Map<String, Any>,
    @SerializedName("duration_seconds") val durationSeconds: Int
)

// Response from interview analysis
data class AnalyzeInterviewResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("analysis") val analysis: InterviewAnalysis?,
    @SerializedName("message_sent") val messageSent: Boolean?,
    @SerializedName("error") val error: String?
)

data class InterviewAnalysis(
    @SerializedName("candidate_name") val candidateName: String,
    @SerializedName("position") val position: String,
    @SerializedName("completion_percentage") val completionPercentage: Int,
    @SerializedName("overall_score") val overallScore: Int, // 0-100
    @SerializedName("strengths") val strengths: List<String>,
    @SerializedName("weaknesses") val weaknesses: List<String>,
    @SerializedName("question_analysis") val questionAnalysis: List<QuestionAnalysis>,
    @SerializedName("recommendation") val recommendation: String, // STRONG_HIRE, HIRE, MAYBE, NO_HIRE
    @SerializedName("summary") val summary: String,
    @SerializedName("interview_duration") val interviewDuration: String
)

data class QuestionAnalysis(
    @SerializedName("question") val question: String,
    @SerializedName("answer") val answer: String,
    @SerializedName("score") val score: Int, // 0-10
    @SerializedName("feedback") val feedback: String
)

class InterviewAnalysisRepository {
    
    private val baseUrl = "https://voice-chatbot-k3fe.onrender.com"
    
    private val okHttpClient = okhttp3.OkHttpClient.Builder()
        .connectTimeout(90, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(90, java.util.concurrent.TimeUnit.SECONDS)
        .writeTimeout(90, java.util.concurrent.TimeUnit.SECONDS)
        .build()
    
    private val retrofit = retrofit2.Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(okHttpClient)
        .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create())
        .build()
    
    private val api = retrofit.create(InterviewAnalysisApi::class.java)
    
    suspend fun analyzeInterview(
        sessionId: String,
        chatId: String,
        userDetails: Map<String, Any>,
        offerDetails: Map<String, Any>,
        durationSeconds: Int
    ): AnalyzeInterviewResponse {
        println("InterviewAnalysis: Sending analysis request for session $sessionId")
        
        return try {
            val request = AnalyzeInterviewRequest(
                sessionId = sessionId,
                chatId = chatId,
                userDetails = userDetails,
                offerDetails = offerDetails,
                durationSeconds = durationSeconds
            )
            
            val response = api.analyzeInterview(request)
            println("InterviewAnalysis: Success - Score: ${response.analysis?.overallScore}")
            
            response
        } catch (e: Exception) {
            println("InterviewAnalysis: Error - ${e.message}")
            e.printStackTrace()
            
            AnalyzeInterviewResponse(
                success = false,
                analysis = null,
                messageSent = false,
                error = e.message ?: "Unknown error"
            )
        }
    }
}

interface InterviewAnalysisApi {
    @retrofit2.http.POST("/api/analyze-interview")
    suspend fun analyzeInterview(
        @retrofit2.http.Body request: AnalyzeInterviewRequest
    ): AnalyzeInterviewResponse
}
