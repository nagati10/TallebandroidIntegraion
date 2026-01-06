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
    val filename: String,
    val username: String,
)

data class UpdateUserRequest(
    val nom: String? = null,
    val email: String? = null,
    val contact: String? = null,
    val image: String? = null,
    val modeExamens: Boolean? = null,
    val is_archive: Boolean? = null,
    val TrustXP: Int? = null,
    val is_Organization: Boolean? = null
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

data class LikedOffresResponse(
    @SerializedName("likedOffres") val likedOffres: List<String>? = null
)

data class IsLikedResponse(
    @SerializedName("isLiked") val isLiked: Boolean? = null
)

// Dans sim2.app.talleb_5edma.network

data class CreateUserRequest(
    // CHANGEMENT : 'fullName' dans Compose, sérialisé en 'nom' pour l'API
    @SerializedName("nom") val nom: String,

    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String,

    // NOUVEAU : Champ pour la validation UI uniquement
    val confirmPassword: String,

    // CHANGEMENT : 'phone' dans Compose, sérialisé en 'contact' pour l'API
    @SerializedName("contact") val contact: String,

    @SerializedName("role") val role: String = "user",
    @SerializedName("image") val image: String = "",
    @SerializedName("is_Organization") val isOrganization: Boolean = false
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

    // Add missing fields
    @SerializedName("modeExamens") val modeExamens: Boolean? = null,
    @SerializedName("is_archive") val isArchive: Boolean? = null,
    @SerializedName("TrustXP") val trustXP: Int? = null,
    @SerializedName("is_Organization") val isOrganization: Boolean? = null,
    
    // CV Profile Fields
    @SerializedName("cvExperience") val cvExperience: List<String>? = null,
    @SerializedName("cvEducation") val cvEducation: List<String>? = null,
    @SerializedName("cvSkills") val cvSkills: List<String>? = null,

    @SerializedName("success") val success: Boolean?,
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: User?,
    @SerializedName("status") val status: Int?
)

// Update this in your network models
data class TrustLevelResponse(
    @SerializedName("level") val level: Int? = null,
    @SerializedName("text") val text: String? = null,
    @SerializedName("message") val message: String? = null
)

data class ExamModeResponse(
    @SerializedName("modeExamens") val modeExamens: Boolean? = null
)

data class ArchiveToggleResponse(
    @SerializedName("is_archive") val isArchive: Boolean? = null,
    @SerializedName("message") val message: String? = null
)

data class OrganizationToggleResponse(
    @SerializedName("is_Organization") val isOrganization: Boolean? = null,
    @SerializedName("message") val message: String? = null
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
            setBody(
                mapOf(
                    "email" to email,
                    "password" to password
                )
            )
        }.body()
    }

    // ==================== SIGNUP FUNCTION / CREATE USER ====================
    suspend fun createUser(
        userRequest: CreateUserRequest,
        imageBytes: ByteArray? = null,
        imageFileName: String? = null
    ): User {
        return client.post("$BASE_URL/admin/register") {
            setBody(
                MultiPartFormDataContent(
                    formData {
                        append("nom", userRequest.nom)
                        append("email", userRequest.email)
                        append("password", userRequest.password)
                        append("contact", userRequest.contact)
                        append("role", userRequest.role)
                        append("is_Organization", userRequest.isOrganization.toString())

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
                            append(HttpHeaders.ContentType, "image/jpeg")
                            append(HttpHeaders.ContentDisposition, "filename=\"$imageFileName\"")
                        })
                    }
                )
            )
        }.body()
    }

    // ==================== ADMIN FUNCTIONS ====================

    suspend fun getAllUsers(token: String): ApiResponse<List<User>> {
        return client.get("$BASE_URL/admin/Get_All_Users") {
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer $token")
        }.body()
    }

    suspend fun deleteAllUsers(token: String): ApiResponse<String> {
        return client.delete("$BASE_URL/admin/Delete_All_Users") {
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer $token")
        }.body()
    }

    suspend fun updateUserById(
        token: String,
        userId: String,
        updateData: UpdateUserRequest
    ): ApiResponse<User> {
        return client.patch("$BASE_URL/admin/$userId") {
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer $token")
            setBody(updateData)
        }.body()
    }

    suspend fun deleteUserById(token: String, userId: String): ApiResponse<User> {
        return client.delete("$BASE_URL/admin/$userId") {
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer $token")
        }.body()
    }

    // ==================== GET Image BY ID ====================
    suspend fun getOtherProfileImage(id: String): ImageResponse {
        return client.get("$BASE_URL/user/me/image/$id") {
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

    // ==================== TRUST & ARCHIVE MANAGEMENT ====================

    suspend fun toggleArchiveState(token: String): User {
        return client.patch("$BASE_URL/user/me/archive/toggle") {
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer $token")
        }.body()
    }

    suspend fun levelUpTrust(token: String, xp: Int): User {
        return client.patch("$BASE_URL/user/me/trust/level-up/$xp") {
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer $token")
        }.body()
    }

    suspend fun levelDownTrust(token: String, xp: Int): User {
        return client.patch("$BASE_URL/user/me/trust/level-down/$xp") {
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer $token")
        }.body()
    }

    suspend fun getTrustLevel(token: String): TrustLevelResponse {
        return client.get("$BASE_URL/user/me/trust/level") {
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer $token")
        }.body()
    }

    suspend fun toggleOrganization(token: String): User {
        return client.patch("$BASE_URL/user/me/organization/toggle") {
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer $token")
        }.body()
    }

    suspend fun getExamMode(token: String): ExamModeResponse {
        return client.get("$BASE_URL/user/me/mode-examens") {
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer $token")
        }.body()
    }

    // ==================== ADMIN METHODS FOR TRUST & ARCHIVE ====================

    suspend fun toggleArchiveStateForUser(token: String, userId: String): ApiResponse<User> {
        return client.patch("$BASE_URL/admin/$userId/archive/toggle") {
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer $token")
        }.body()
    }

    suspend fun levelUpTrustForUser(token: String, userId: String, xp: Int): ApiResponse<User> {
        return client.patch("$BASE_URL/admin/$userId/trust/level-up/$xp") {
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer $token")
        }.body()
    }

    suspend fun levelDownTrustForUser(token: String, userId: String, xp: Int): ApiResponse<User> {
        return client.patch("$BASE_URL/admin/$userId/trust/level-down/$xp") {
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer $token")
        }.body()
    }

    suspend fun getTrustLevelForUser(
        token: String,
        userId: String
    ): ApiResponse<TrustLevelResponse> {
        return client.get("$BASE_URL/user/me/$userId/trust/level") {
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer $token")
        }.body()
    }

    suspend fun toggleOrganizationForUser(token: String, userId: String): ApiResponse<User> {
        return client.patch("$BASE_URL/admin/$userId/organization/toggle") {
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer $token")
        }.body()
    }

    // ==================== LIKED OFFERS MANAGEMENT ====================

    suspend fun getLikedOffres(token: String): LikedOffresResponse {
        return client.get("$BASE_URL/user/me/liked-offres") {
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer $token")
        }.body()
    }

    suspend fun addLikedOffre(token: String, offreId: String): User {
        return client.post("$BASE_URL/user/me/like-offre/$offreId") {
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer $token")
        }.body()
    }

    suspend fun removeLikedOffre(token: String, offreId: String): User {
        return client.delete("$BASE_URL/user/me/unlike-offre/$offreId") {
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer $token")
        }.body()
    }

    suspend fun isOffreLiked(token: String, offreId: String): IsLikedResponse {
        return client.get("$BASE_URL/user/me/is-offre-liked/$offreId") {
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer $token")
        }.body()
    }

    // ==================== EXAM MODE TOGGLE ====================

    suspend fun toggleExamMode(token: String): User {
        return client.patch("$BASE_URL/user/me/mode-examens/toggle") {
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer $token")
        }.body()
    }

    // ==================== CV → UPDATE PROFILE FROM CV ====================

    suspend fun updateProfileFromCv(
        token: String,
        body: CreateProfileFromCvRequest
    ): User {
        return client.patch("$BASE_URL/user/me/cv/profile") {
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer $token")
            setBody(body)
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
