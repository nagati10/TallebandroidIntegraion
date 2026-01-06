package sim2.app.talleb_5edma.models

import com.google.gson.annotations.SerializedName

// ==================== ENUMS ====================

// ==================== ENUMS ====================
enum class MessageType {
    @SerializedName("text") TEXT,
    @SerializedName("image") IMAGE,
    @SerializedName("video") VIDEO,
    @SerializedName("audio") AUDIO,
    @SerializedName("emoji") EMOJI,
    @SerializedName("interview_result") INTERVIEW_RESULT
}



// ==================== REQUEST MODELS ====================
data class CreateChatRequest(
    @SerializedName("entreprise") val entreprise: String,
    @SerializedName("offer") val offer: String
)

data class SendMessageRequest(
    @SerializedName("content") val content: String? = null,
    @SerializedName("type") val type: MessageType,
    @SerializedName("mediaUrl") val mediaUrl: String? = null,
    @SerializedName("fileName") val fileName: String? = null,
    @SerializedName("fileSize") val fileSize: String? = null,
    @SerializedName("duration") val duration: String? = null
)

data class BlockChatRequest(
    @SerializedName("blockReason") val blockReason: String? = null
)

// ==================== UNIQUE RESPONSE MODELS - ONE PER ENDPOINT ====================

// Response for createOrGetChat
data class CreateChatResponse(
    @SerializedName("_id") val id: String,
    @SerializedName("candidate") val candidate: String? = null,
    @SerializedName("entreprise") val entreprise: String? = null,
    @SerializedName("offer") val offer: String? = null,
    @SerializedName("isBlocked") val isBlocked: Boolean? = false,
    @SerializedName("blockedBy") val blockedBy: String? = null,
    @SerializedName("blockReason") val blockReason: String? = null,
    @SerializedName("isDeleted") val isDeleted: Boolean? = false,
    @SerializedName("deletedBy") val deletedBy: String? = null,
    @SerializedName("isAccepted") val isAccepted: Boolean? = false,
    @SerializedName("acceptedAt") val acceptedAt: String? = null,
    @SerializedName("lastActivity") val lastActivity: String? = null,
    @SerializedName("lastMessage") val lastMessage: String? = null,
    @SerializedName("lastMessageType") val lastMessageType: String? = null,
    @SerializedName("unreadCandidate") val unreadCandidate: Int? = 0,
    @SerializedName("unreadEntreprise") val unreadEntreprise: Int? = 0,
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("updatedAt") val updatedAt: String? = null
)

// Response for getChatById
data class GetChatByIdResponse(
    @SerializedName("_id") val id: String,
    @SerializedName("candidate") val candidate: ChatUser? = null,
    @SerializedName("entreprise") val entreprise: ChatUser? = null,
    @SerializedName("offer") val offer: Offre? = null,
    @SerializedName("isBlocked") val isBlocked: Boolean? = false,
    @SerializedName("blockedBy") val blockedBy: String? = null,
    @SerializedName("blockReason") val blockReason: String? = null,
    @SerializedName("isDeleted") val isDeleted: Boolean? = false,
    @SerializedName("deletedBy") val deletedBy: String? = null,
    @SerializedName("isAccepted") val isAccepted: Boolean? = false,
    @SerializedName("acceptedAt") val acceptedAt: String? = null,
    @SerializedName("lastActivity") val lastActivity: String? = null,
    @SerializedName("lastMessage") val lastMessage: String? = null,
    @SerializedName("lastMessageType") val lastMessageType: String? = null,
    @SerializedName("unreadCandidate") val unreadCandidate: Int? = 0,
    @SerializedName("unreadEntreprise") val unreadEntreprise: Int? = 0,
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("updatedAt") val updatedAt: String? = null
)

// Response for getUserChats
data class GetUserChatsResponse(
    @SerializedName("_id") val id: String,
    @SerializedName("candidate") val candidate: ChatUser? = null,
    @SerializedName("entreprise") val entreprise: ChatUser? = null,
    @SerializedName("offer") val offer: Offre? = null,
    @SerializedName("isBlocked") val isBlocked: Boolean? = false,
    @SerializedName("isAccepted") val isAccepted: Boolean? = false,
    @SerializedName("lastActivity") val lastActivity: String? = null,
    @SerializedName("lastMessage") val lastMessage: String? = null,
    @SerializedName("lastMessageType") val lastMessageType: String? = null,
    @SerializedName("unreadCandidate") val unreadCandidate: Int? = 0,
    @SerializedName("unreadEntreprise") val unreadEntreprise: Int? = 0
)

// Response for sendMessage
data class SendMessageResponse(
    @SerializedName("_id") val id: String,
    @SerializedName("chat") val chat: String,
    @SerializedName("sender") val sender: ChatUser? = null,
    @SerializedName("type") val type: MessageType,
    @SerializedName("content") val content: String? = null,
    @SerializedName("mediaUrl") val mediaUrl: String? = null,
    @SerializedName("fileName") val fileName: String? = null,
    @SerializedName("fileSize") val fileSize: String? = null,
    @SerializedName("duration") val duration: String? = null,
    @SerializedName("isRead") val isRead: Boolean? = false,
    @SerializedName("createdAt") val createdAt: String? = null
)

// Response for getChatMessages
data class GetChatMessagesResponse(
    @SerializedName("messages") val messages: List<ChatMessage>,
    @SerializedName("total") val total: Int
)

// Response for blockChat
data class BlockChatResponse(
    @SerializedName("_id") val id: String,
    @SerializedName("candidate") val candidate: ChatUser? = null,
    @SerializedName("entreprise") val entreprise: ChatUser? = null,
    @SerializedName("offer") val offer: Offre? = null,
    @SerializedName("isBlocked") val isBlocked: Boolean? = false,
    @SerializedName("blockedBy") val blockedBy: String? = null,
    @SerializedName("blockReason") val blockReason: String? = null,
    @SerializedName("isDeleted") val isDeleted: Boolean? = false,
    @SerializedName("deletedBy") val deletedBy: String? = null,
    @SerializedName("isAccepted") val isAccepted: Boolean? = false,
    @SerializedName("acceptedAt") val acceptedAt: String? = null,
    @SerializedName("lastActivity") val lastActivity: String? = null,
    @SerializedName("lastMessage") val lastMessage: String? = null,
    @SerializedName("lastMessageType") val lastMessageType: String? = null,
    @SerializedName("unreadCandidate") val unreadCandidate: Int? = 0,
    @SerializedName("unreadEntreprise") val unreadEntreprise: Int? = 0,
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("updatedAt") val updatedAt: String? = null
)

// Response for unblockChat
data class UnblockChatResponse(
    @SerializedName("_id") val id: String,
    @SerializedName("candidate") val candidate: ChatUser? = null,
    @SerializedName("entreprise") val entreprise: ChatUser? = null,
    @SerializedName("offer") val offer: Offre? = null,
    @SerializedName("isBlocked") val isBlocked: Boolean? = false,
    @SerializedName("blockedBy") val blockedBy: String? = null,
    @SerializedName("blockReason") val blockReason: String? = null,
    @SerializedName("isDeleted") val isDeleted: Boolean? = false,
    @SerializedName("deletedBy") val deletedBy: String? = null,
    @SerializedName("isAccepted") val isAccepted: Boolean? = false,
    @SerializedName("acceptedAt") val acceptedAt: String? = null,
    @SerializedName("lastActivity") val lastActivity: String? = null,
    @SerializedName("lastMessage") val lastMessage: String? = null,
    @SerializedName("lastMessageType") val lastMessageType: String? = null,
    @SerializedName("unreadCandidate") val unreadCandidate: Int? = 0,
    @SerializedName("unreadEntreprise") val unreadEntreprise: Int? = 0,
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("updatedAt") val updatedAt: String? = null
)

// Response for acceptCandidate
data class AcceptCandidateResponse(
    @SerializedName("_id") val id: String,
    @SerializedName("candidate") val candidate: ChatUser? = null,
    @SerializedName("entreprise") val entreprise: ChatUser? = null,
    @SerializedName("offer") val offer: Offre? = null,
    @SerializedName("isBlocked") val isBlocked: Boolean? = false,
    @SerializedName("blockedBy") val blockedBy: String? = null,
    @SerializedName("blockReason") val blockReason: String? = null,
    @SerializedName("isDeleted") val isDeleted: Boolean? = false,
    @SerializedName("deletedBy") val deletedBy: String? = null,
    @SerializedName("isAccepted") val isAccepted: Boolean? = false,
    @SerializedName("acceptedAt") val acceptedAt: String? = null,
    @SerializedName("lastActivity") val lastActivity: String? = null,
    @SerializedName("lastMessage") val lastMessage: String? = null,
    @SerializedName("lastMessageType") val lastMessageType: String? = null,
    @SerializedName("unreadCandidate") val unreadCandidate: Int? = 0,
    @SerializedName("unreadEntreprise") val unreadEntreprise: Int? = 0,
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("updatedAt") val updatedAt: String? = null
)

// Response for markMessagesAsRead
data class MarkMessagesReadResponse(
    @SerializedName("message") val message: String
)

// Response for deleteChat
data class DeleteChatResponse(
    @SerializedName("message") val message: String
)

// Response for uploadMedia
data class UploadMediaResponse(
    @SerializedName("url") val url: String,
    @SerializedName("fileName") val fileName: String,
    @SerializedName("fileSize") val fileSize: String
)

// ==================== SUB-MODELS ====================
data class ChatUser(
    @SerializedName("_id") val id: String,
    @SerializedName("nom") val nom: String,
    @SerializedName("email") val email: String,
    @SerializedName("contact") val contact: String? = null,
    @SerializedName("image") val image: String? = null,
    @SerializedName("is_Organization") val isOrganization: Boolean? = false
)


data class ChatLocation(
    @SerializedName("address") val address: String? = null,
    @SerializedName("city") val city: String? = null,
    @SerializedName("country") val country: String? = null
)

data class ChatMessage(
    @SerializedName("_id") val id: String,
    @SerializedName("chat") val chat: String,
    @SerializedName("sender") val sender: ChatUser? = null,
    @SerializedName("type") val type: MessageType,
    @SerializedName("content") val content: String? = null,
    @SerializedName("mediaUrl") val mediaUrl: String? = null,
    @SerializedName("fileName") val fileName: String? = null,
    @SerializedName("fileSize") val fileSize: String? = null,
    @SerializedName("duration") val duration: String? = null,
    @SerializedName("isRead") val isRead: Boolean? = false,
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("interviewAnalysis") val interviewAnalysis: InterviewAnalysisData? = null
)

data class InterviewAnalysisData(
    @SerializedName("candidateName") val candidateName: String,
    @SerializedName("position") val position: String,
    @SerializedName("completionPercentage") val completionPercentage: Int,
    @SerializedName("overallScore") val overallScore: Int,
    @SerializedName("strengths") val strengths: List<String>,
    @SerializedName("weaknesses") val weaknesses: List<String>,
    @SerializedName("recommendation") val recommendation: String,
    @SerializedName("summary") val summary: String,
    @SerializedName("interviewDuration") val interviewDuration: String,
    @SerializedName("questionAnalysis") val questionAnalysis: List<QuestionAnalysis> = emptyList()
)

data class QuestionAnalysis(
    @SerializedName("question") val question: String,
    @SerializedName("answer") val answer: String,
    @SerializedName("score") val score: Int,
    @SerializedName("feedback") val feedback: String
)

// ==================== UI HELPERS ====================
sealed class ChatListItem {
    data class MessageItem(val message: ChatMessage) : ChatListItem()
    data class TimeSeparatorItem(val separator: TimeSeparator) : ChatListItem()
}

data class TimeSeparator(
    val text: String,
    val timestamp: String
)

// Extension function to group messages with time separators
fun List<ChatMessage>.groupWithTimeSeparators(): List<ChatListItem> {
    val items = mutableListOf<ChatListItem>()
    var lastDate: String? = null

    this.sortedBy { it.createdAt }.forEach { message ->
        val messageDate = message.createdAt?.take(10) // Get YYYY-MM-DD part

        if (messageDate != null && messageDate != lastDate) {
            items.add(ChatListItem.TimeSeparatorItem(TimeSeparator(
                text = formatDateForDisplay(messageDate),
                timestamp = messageDate
            )))
            lastDate = messageDate
        }
        items.add(ChatListItem.MessageItem(message))
    }

    return items
}

private fun formatDateForDisplay(dateString: String): String {
    return try {
        val inputFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        val date = inputFormat.parse(dateString) ?: return dateString

        val now = java.util.Calendar.getInstance()
        val target = java.util.Calendar.getInstance().apply { time = date }

        when {
            now.get(java.util.Calendar.YEAR) == target.get(java.util.Calendar.YEAR) &&
                    now.get(java.util.Calendar.DAY_OF_YEAR) == target.get(java.util.Calendar.DAY_OF_YEAR) -> "Today"
            now.get(java.util.Calendar.YEAR) == target.get(java.util.Calendar.YEAR) &&
                    now.get(java.util.Calendar.DAY_OF_YEAR) == target.get(java.util.Calendar.DAY_OF_YEAR) + 1 -> "Yesterday"
            else -> {
                val outputFormat = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
                outputFormat.format(date)
            }
        }
    } catch (e: Exception) {
        dateString
    }
}

// Extension function to get display time for messages
fun ChatMessage.getDisplayTime(): String {
    return try {
        val inputFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.getDefault())
        val date = inputFormat.parse(this.createdAt ?: return "")
        val outputFormat = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
        outputFormat.format(date)
    } catch (e: Exception) {
        ""
    }
}