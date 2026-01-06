package sim2.app.talleb_5edma.models

import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("_id") val _id: String? = null,
    @SerializedName("nom") val nom: String? = null,
    @SerializedName("email") val email: String? = null,
    @SerializedName("contact") val contact: String? = null,
    @SerializedName("role") val role: String? = null,
    @SerializedName("image") val image: String? = null,
    @SerializedName("password") val password: String? = null,
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("updatedAt") val updatedAt: String? = null,

    // Chat-related fields
    @SerializedName("myChats") val myChats: List<String>? = null, // Array of chat IDs
    @SerializedName("blockedUsers") val blockedUsers: List<String>? = null, // Users I blocked
    @SerializedName("blockedBy") val blockedBy: List<String>? = null, // Users who blocked me
    @SerializedName("callPermissions") val callPermissions: Boolean? = null, // Global call permission
    @SerializedName("lastSeen") val lastSeen: String? = null,
    @SerializedName("isOnline") val isOnline: Boolean? = null,

    // Existing fields
    @SerializedName("modeExamens") val modeExamens: Boolean? = null,
    @SerializedName("is_archive") val isArchive: Boolean? = null,
    @SerializedName("TrustXP") val trustXP: Int? = null,
    @SerializedName("is_Organization") val isOrganization: Boolean? = null,
    @SerializedName("likedOffres") val likedOffres: List<String>? = null,
    
    // CV Profile Fields from AI
    @SerializedName("cvExperience") val cvExperience: List<String>? = null,
    @SerializedName("cvEducation") val cvEducation: List<String>? = null,
    @SerializedName("cvSkills") val cvSkills: List<String>? = null
)