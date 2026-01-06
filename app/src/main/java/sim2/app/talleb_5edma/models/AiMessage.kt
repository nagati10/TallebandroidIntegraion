package sim2.app.talleb_5edma.models

import java.util.UUID

/**
 * Message model for AI interview training chat
 */
data class AiMessage(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val isUser: Boolean,
    val audioUrl: String? = null, // For user recordings (local file path)
    val audioData: ByteArray? = null, // For AI responses (base64 decoded MP3 data)
    val timestamp: Long = System.currentTimeMillis(),
    val isPlaying: Boolean = false // For audio playback state
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AiMessage

        if (id != other.id) return false
        if (text != other.text) return false
        if (isUser != other.isUser) return false
        if (audioUrl != other.audioUrl) return false
        if (audioData != null) {
            if (other.audioData == null) return false
            if (!audioData.contentEquals(other.audioData)) return false
        } else if (other.audioData != null) return false
        if (timestamp != other.timestamp) return false
        if (isPlaying != other.isPlaying) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + text.hashCode()
        result = 31 * result + isUser.hashCode()
        result = 31 * result + (audioUrl?.hashCode() ?: 0)
        result = 31 * result + (audioData?.contentHashCode() ?: 0)
        result = 31 * result + timestamp.hashCode()
        result = 31 * result + isPlaying.hashCode()
        return result
    }
}
