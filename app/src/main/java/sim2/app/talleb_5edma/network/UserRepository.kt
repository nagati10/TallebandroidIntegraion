package sim2.app.talleb_5edma.network

import com.google.gson.annotations.SerializedName
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.delete
import io.ktor.client.request.*
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.takeFrom
import sim2.app.talleb_5edma.models.User
import sim2.app.talleb_5edma.util.*

// ==================== DATA CLASSES FOR RESPONSES ====================
data class LoginResponse(
    val status: String? = null,
    val message: String? = null,
    val access_token: String? = null,
    val user: User? = null
)

data class ImageResponse(
    val imageUrl: String,
    val filename: String
)

data class UpdateUserRequest(
    val nom: String? = null,
    val email: String? = null,
    val contact: String? = null,
    val image: String? = null
)

data class EmailExistsResponse(
    val exists: Boolean
)

data class ResetPasswordRequest(
    @SerializedName("email") val email: String,
    @SerializedName("newPassword") val newPassword: String
)

data class ResetPasswordResponse(
    val message: String
)

data class CreateUserRequest(
    @SerializedName("nom") val nom: String,
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String,
    @SerializedName("contact") val contact: String,
    @SerializedName("role") val role: String = "user",
    @SerializedName("image") val image: String = ""
)

data class GoogleLoginRequest(
    @SerializedName("id_token") val idToken: String
)

data class GetCurrentUser(
    @SerializedName("_id") val _id: String?,
    @SerializedName("nom") val nom: String,
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String,
    @SerializedName("role") val role: String,
    @SerializedName("contact") val contact: String,
    @SerializedName("image") val image: String? = null,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("updatedAt") val updatedAt: String,
    @SerializedName("success") val success: Boolean?,
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: User?,
    @SerializedName("status") val status: Int?
)

class UserRepository {

    suspend fun loginWithGoogle(idToken: String): LoginResponse {
        return client.post("$BASE_URL/auth/google") {
            contentType(ContentType.Application.Json)
            setBody(GoogleLoginRequest(idToken))
        }.body()
    }

    // ==================== LOGIN FUNCTION ====================
    suspend fun login(email: String, password: String): LoginResponse {
        return client.post("$BASE_URL/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(mapOf(
                "email" to email,
                "password" to password
            ))
        }.body()
    }

    // ==================== SIGNUP FUNCTION ====================
    suspend fun signup(
        userRequest: CreateUserRequest,
        imageBytes: ByteArray? = null,
        imageFileName: String? = null
    ): User {
        return client.post("$BASE_URL/admin/register") {
            setBody(MultiPartFormDataContent(
                formData {
                    // Append all the text fields
                    append("nom", userRequest.nom)
                    append("email", userRequest.email)
                    append("password", userRequest.password)
                    append("contact", userRequest.contact)
                    append("role", userRequest.role)

                    // Append the image file if it exists
                    if (imageBytes != null && imageFileName != null) {
                        append("image", imageBytes, Headers.build {
                            append(HttpHeaders.ContentType, "image/jpeg") // Or png, etc.
                            append(HttpHeaders.ContentDisposition, "filename=\"$imageFileName\"")
                        })
                    }
                }
            ))
        }.body()
    }

    // ==================== UPDATE USER PROFILE ====================
    suspend fun updateUser(token: String, updateData: UpdateUserRequest): User {
        return client.patch("$BASE_URL/user/me") {
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer $token")
            setBody(updateData)
        }.body()
    }

    // ==================== UPDATE USER PROFILE (with Map) ====================
    suspend fun updateUser(token: String, updateData: Map<String, Any>): User {
        return client.patch("$BASE_URL/user/me") {
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer $token")
            setBody(updateData)
        }.body()
    }

    // ==================== DELETE USER PROFILE ====================
    suspend fun deleteUser(token: String): ApiResponse<User> {
        return client.delete("$BASE_URL/user/me") {
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer $token")
        }.body()
    }

    // ==================== GET USER PROFILE IMAGE ====================
    suspend fun getProfileImage(token: String): ImageResponse {
        return client.get("$BASE_URL/user/me/image/Get") {
            header("Authorization", "Bearer $token")
        }.body()
    }

    // ==================== UPDATE PROFILE IMAGE ====================
    suspend fun updateProfileImage(
        token: String,
        imageBytes: ByteArray,
        imageFileName: String
    ): User {
        return client.patch("$BASE_URL/user/me/image/update") {
            header("Authorization", "Bearer $token")
            setBody(
                MultiPartFormDataContent(
                    formData {
                        append("image", imageBytes, Headers.build {
                            append(
                                HttpHeaders.ContentType,
                                "image/jpeg"
                            ) // Adjust content type as needed
                            append(HttpHeaders.ContentDisposition, "filename=\"$imageFileName\"")
                        })
                    }
                ))
        }.body()
    }

    // ==================== ADMIN FUNCTIONS ====================

    // Get all users (admin only)
    suspend fun getAllUsers(token: String): ApiResponse<List<User>> {
        return client.get("$BASE_URL/admin/Get_All_Users") {
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer $token")
        }.body()
    }

    // Delete all users (admin only)
    suspend fun deleteAllUsers(token: String): ApiResponse<String> {
        return client.delete("$BASE_URL/admin/Delete_All_Users") {
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer $token")
        }.body()
    }

    // Update any user (admin only)
    suspend fun updateUserById(token: String, userId: String, updateData: UpdateUserRequest): ApiResponse<User> {
        return client.patch("$BASE_URL/admin/$userId") {
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer $token")
            setBody(updateData)
        }.body()
    }

    // Delete any user (admin only)
    suspend fun deleteUserById(token: String, userId: String): ApiResponse<User> {
        return client.delete("$BASE_URL/admin/$userId") {
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer $token")
        }.body()
    }

    // ==================== GET USER BY ID ====================
    suspend fun getUserById(id: String, token: String? = null): ApiResponse<User> {
        return client.get("$BASE_URL/api/users/$id") {
            token?.let {
                header("Authorization", "Bearer $it")
            }
        }.body()
    }

    // ==================== FIND USER BY EMAIL ====================
    suspend fun findUserByEmail(token: String, email: String): ApiResponse<User> {
        return client.get("$BASE_URL/admin/user-by-email/$email") {
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer $token")
        }.body()
    }

    // ==================== GET CURRENT USER PROFILE ====================
    suspend fun getCurrentUser(token: String): GetCurrentUser {
        return client.get("$BASE_URL/user/me") {
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer $token")
        }.body()
    }

    // ==================== CHECK IF EMAIL EXISTS ====================
    suspend fun checkEmailExists(email: String): EmailExistsResponse {
        return client.get("$BASE_URL/admin/email-exists/$email") {
            contentType(ContentType.Application.Json)
        }.body()
    }

    // ==================== RESET PASSWORD ====================
    suspend fun resetPassword(email: String, newPassword: String): ResetPasswordResponse {
        return client.patch("$BASE_URL/user/me/reset-password") {
            contentType(ContentType.Application.Json)
            setBody(ResetPasswordRequest(email, newPassword))
        }.body()
    }
}

// ==================== EXTENSION FOR DELETE REQUEST ====================
suspend inline fun <reified T> HttpClient.delete(
    urlString: String,
    block: HttpRequestBuilder.() -> Unit = {}
): T = delete {
    url.takeFrom(urlString)
    apply(block)
}.body()