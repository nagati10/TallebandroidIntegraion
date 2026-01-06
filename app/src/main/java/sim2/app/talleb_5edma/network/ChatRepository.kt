package sim2.app.talleb_5edma.network

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.provider.OpenableColumns
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import io.ktor.client.request.delete
import sim2.app.talleb_5edma.util.BASE_URL
import sim2.app.talleb_5edma.util.client
import io.ktor.client.call.body
import io.ktor.client.request.*
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.http.*
import sim2.app.talleb_5edma.models.*

class ChatRepository() {

    // POST /chat - Create or get chat
    suspend fun createOrGetChat(token: String, request: CreateChatRequest): CreateChatResponse {
        val response = client.post("$BASE_URL/chat") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(request)
        }

        return if (response.status == HttpStatusCode.Created || response.status == HttpStatusCode.OK) {
            response.body()
        } else {
            throw Exception("Failed to create chat: ${response.status}")
        }
    }

    // GET /chat/:chatId - Get chat by ID
    suspend fun getChatById(token: String, chatId: String): GetChatByIdResponse {
        val response = client.get("$BASE_URL/chat/$chatId") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        return response.body()
    }

    // GET /chat/my-chats - Get user chats
    suspend fun getMyChats(token: String): List<GetUserChatsResponse> {
        val response = client.get("$BASE_URL/chat/my-chats") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        return response.body()
    }

    // GET /chat/:chatId/messages - Get chat messages
    suspend fun getMessages(token: String, chatId: String): GetChatMessagesResponse {
        val response = client.get("$BASE_URL/chat/$chatId/messages") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        return response.body()
    }

    // POST /chat/:chatId/message - Send message
    suspend fun sendMessage(token: String, chatId: String, request: SendMessageRequest): SendMessageResponse {
        val response = client.post("$BASE_URL/chat/$chatId/message") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        return response.body()
    }

    // PATCH /chat/:chatId/block - Block chat
    suspend fun blockChat(token: String, chatId: String, reason: String? = null): BlockChatResponse {
        val response = client.patch("$BASE_URL/chat/$chatId/block") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(BlockChatRequest(blockReason = reason))
        }
        return response.body()
    }

    // PATCH /chat/:chatId/unblock - Unblock chat
    suspend fun unblockChat(token: String, chatId: String): UnblockChatResponse {
        val response = client.patch("$BASE_URL/chat/$chatId/unblock") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        return response.body()
    }

    // PATCH /chat/:chatId/accept - Accept candidate
    suspend fun acceptCandidate(token: String, chatId: String): AcceptCandidateResponse {
        val response = client.patch("$BASE_URL/chat/$chatId/accept") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        return response.body()
    }

    // PATCH /chat/:chatId/mark-read - Mark messages as read
    suspend fun markMessagesAsRead(token: String, chatId: String): MarkMessagesReadResponse {
        val response = client.patch("$BASE_URL/chat/$chatId/mark-read") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        return response.body()
    }

    // GET /chat/can-call/:offerId - Check call permission
    suspend fun canMakeCall(token: String, offerId: String): Boolean {
        val response = client.get("$BASE_URL/chat/can-call/$offerId") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }

        return when {
            response.status.isSuccess() -> {
                response.body<String>().toBoolean()
            }
            else -> false // or handle error appropriately
        }
    }

    // DELETE /chat/:chatId - Delete chat
    suspend fun deleteChat(token: String, chatId: String): DeleteChatResponse {
        val response = client.delete("$BASE_URL/chat/$chatId") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        return response.body()
    }

    // Helper to upload media
    suspend fun uploadMedia(token: String, fileBytes: ByteArray, fileName: String): UploadMediaResponse {
        val response = client.post("$BASE_URL/chat/upload") {
            header(HttpHeaders.Authorization, "Bearer $token")
            header(HttpHeaders.Accept, "*/*")
            setBody(MultiPartFormDataContent(
                formData {
                    append(
                        key = "file",
                        value = fileBytes,
                        headers = Headers.build {
                            append(HttpHeaders.ContentType, getContentType(fileName))
                            append(HttpHeaders.ContentDisposition, "filename=\"$fileName\"")
                        }
                    )
                }
            ))
        }

        return response.body()
    }

    private fun getContentType(fileName: String): String {
        return when (val extension = fileName.substringAfterLast('.').lowercase()) {
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "gif" -> "image/gif"
            "mp4" -> "video/mp4"
            "avi" -> "video/x-msvideo"
            "mov" -> "video/quicktime"
            "mp3" -> "audio/mpeg"
            "wav" -> "audio/wav"
            "ogg" -> "audio/ogg"
            else -> "application/octet-stream"
        }
    }

    suspend fun sendVoiceMessage(
        token: String,
        chatId: String,
        audioBytes: ByteArray,
        fileName: String,
        duration: String
    ): SendMessageResponse {
        val uploadResponse = uploadMedia(token, audioBytes, fileName)

        return sendMessage(
            token,
            chatId,
            SendMessageRequest(
                content = null,
                type = MessageType.AUDIO,
                mediaUrl = uploadResponse.url,
                fileName = fileName,
                fileSize = uploadResponse.fileSize,
                duration = duration
            )
        )
    }

    suspend fun sendImageMessage(
        token: String,
        chatId: String,
        imageBytes: ByteArray,
        fileName: String
    ): SendMessageResponse {
        val uploadResponse = uploadMedia(token, imageBytes, fileName)

        return sendMessage(
            token,
            chatId,
            SendMessageRequest(
                content = null,
                type = MessageType.IMAGE,
                mediaUrl = uploadResponse.url,
                fileName = fileName,
                fileSize = uploadResponse.fileSize
            )
        )
    }
}

fun getFileNameFromUri(context : Context, uri: Uri): String? {
    return when (uri.scheme) {
        "content" -> {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val displayNameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (displayNameIndex != -1) cursor.getString(displayNameIndex) else null
                } else null
            }
        }
        "file" -> uri.lastPathSegment
        else -> null
    }
}


fun determineMessageType(fileName: String): MessageType {
    val extension = fileName.substringAfterLast('.').lowercase()
    return when (extension) {
        "jpg", "jpeg", "png", "gif", "bmp", "webp" -> MessageType.IMAGE
        "mp4", "avi", "mov", "mkv", "webm", "3gp" -> MessageType.VIDEO
        else -> MessageType.IMAGE // Fallback to IMAGE for unknown types
    }
}

// Helper function to try and get a path from a URI
fun getPathFromUri(context: Context, uri: Uri): String? {
    val projection = arrayOf(MediaStore.MediaColumns.DATA)
    try {
        val cursor = context.contentResolver.query(uri, projection, null, null, null)
        return cursor?.use {
            if (it.moveToFirst()) {
                val columnIndex = it.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)
                it.getString(columnIndex)
            } else {
                null
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
        return null
    }
}