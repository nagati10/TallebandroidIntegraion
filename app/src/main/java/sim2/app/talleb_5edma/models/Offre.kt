package sim2.app.talleb_5edma.models

import com.google.gson.annotations.SerializedName

data class Coordinates(
    @SerializedName("lat") val lat: Double? = null,
    @SerializedName("lng") val lng: Double? = null
)

data class Location(
    @SerializedName("address") val address: String? = null,
    @SerializedName("city") val city: String? = null,
    @SerializedName("country") val country: String? = null,
    @SerializedName("coordinates") val coordinates: Coordinates? = null
)

enum class JobType {
    @SerializedName("job") JOB,
    @SerializedName("stage") STAGE,
    @SerializedName("freelance") FREELANCE
}

enum class Shift {
    @SerializedName("jour") JOUR,
    @SerializedName("nuit") NUIT,
    @SerializedName("flexible") FLEXIBLE
}

data class OffreError(
    @SerializedName("field") val field: String? = null,
    @SerializedName("message") val message: String? = null
)


data class Offre(
    @SerializedName("_id") val id: String? = null,
    @SerializedName("title") val title: String? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("reference") val reference: String? = null,
    @SerializedName("tags") val tags: List<String>? = null,
    @SerializedName("exigences") val exigences: List<String>? = null,
    @SerializedName("location") val location: Location? = null,
    @SerializedName("category") val category: String? = null,
    @SerializedName("salary") val salary: String? = null,
    @SerializedName("company") val company: String? = null,
    @SerializedName("expiresAt") val expiresAt: String? = null,
    @SerializedName("jobType") val jobType: JobType? = null,
    @SerializedName("shift") val shift: Shift? = null,
    @SerializedName("isActive") val isActive: Boolean? = null,
    @SerializedName("imageFiles") val imageFiles: List<String>? = null,
    @SerializedName("viewCount") val viewCount: Int? = null,
    @SerializedName("likeCount") val likeCount: Int? = null,
    @SerializedName("likedBy") val likedBy: List<String>? = null,
    @SerializedName("days") val days: Int? = null,
    @SerializedName("createdBy") val createdBy: User? = null,
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("updatedAt") val updatedAt: String? = null,
    @SerializedName("message") val message: String? = null,

    @SerializedName("statusCode") val statusCode: Int? = null,
    @SerializedName("errors") val errors: List<OffreError>? = null,
    @SerializedName("images") val images: List<String>? = null,

    // NEW: Chat & Candidate Management
    @SerializedName("acceptedUsers") val acceptedUsers: List<String>? = null,
    @SerializedName("blockedUsers") val blockedUsers: List<String>? = null,
    @SerializedName("maxCandidates") val maxCandidates: Int? = null,
    @SerializedName("chatEnabled") val chatEnabled: Boolean? = true,
    @SerializedName("allowCalls") val allowCalls: Boolean? = true,
    @SerializedName("callPermissions") val callPermissions: List<String>? = null
)

data class Offre2(
    @SerializedName("_id") val id: String? = null,
    @SerializedName("title") val title: String? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("tags") val tags: List<String>? = null,
    @SerializedName("exigences") val exigences: List<String>? = null,
    @SerializedName("location") val location: Location? = null,
    @SerializedName("category") val category: String? = null,
    @SerializedName("salary") val salary: String? = null,
    @SerializedName("company") val company: String? = null,
    @SerializedName("expiresAt") val expiresAt: String? = null,
    @SerializedName("jobType") val jobType: JobType? = null,
    @SerializedName("shift") val shift: Shift? = null,
    @SerializedName("isActive") val isActive: Boolean? = null,
    @SerializedName("imageFiles") val imageFiles: List<String>? = null,
    @SerializedName("viewCount") val viewCount: Int? = null,
    @SerializedName("likeCount") val likeCount: Int? = null,
    @SerializedName("likedBy") val likedBy: List<String>? = null,
    @SerializedName("days") val days: Int? = null,
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("updatedAt") val updatedAt: String? = null,
    @SerializedName("message") val message: String? = null,

    @SerializedName("statusCode") val statusCode: Int? = null,
    @SerializedName("errors") val errors: List<OffreError>? = null,
    @SerializedName("images") val images: List<String>? = null

)

// Response wrappers
data class OffresResponse(
    @SerializedName("data") val data: List<Offre>? = null,
    @SerializedName("success") val success: Boolean? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("status") val status: Int? = null
)

data class OffreResponse(
    @SerializedName("data") val data: Offre? = null,
    @SerializedName("success") val success: Boolean? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("status") val status: Int? = null
)

data class LikeResponse(
    @SerializedName("liked") val liked: Boolean? = null,
    @SerializedName("likeCount") val likeCount: Int? = null,
    @SerializedName("message") val message: String? = null
)

// Add this to your Offre.kt file
data class CreateOffreRequest(
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String,
    @SerializedName("reference") val reference: String? = null,
    @SerializedName("tags") val tags: List<String>? = null,
    @SerializedName("exigences") val exigences: List<String>? = null,
    @SerializedName("location") val location: Location,
    @SerializedName("category") val category: String? = null,
    @SerializedName("salary") val salary: String? = null,
    @SerializedName("company") val company: String,
    @SerializedName("expiresAt") val expiresAt: String? = null,
    @SerializedName("jobType") val jobType: JobType? = null,
    @SerializedName("shift") val shift: Shift? = null,
    @SerializedName("isActive") val isActive: Boolean? = true,
    @SerializedName("imageFiles") val imageFiles: List<String>? = null
)

data class UpdateOffreRequest(
    @SerializedName("title") val title: String? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("reference") val reference: String? = null,
    @SerializedName("tags") val tags: List<String>? = null,
    @SerializedName("exigences") val exigences: List<String>? = null,
    @SerializedName("location") val location: Location? = null,
    @SerializedName("category") val category: String? = null,
    @SerializedName("salary") val salary: String? = null,
    @SerializedName("company") val company: String? = null,
    @SerializedName("expiresAt") val expiresAt: String? = null,
    @SerializedName("jobType") val jobType: JobType? = null,
    @SerializedName("shift") val shift: Shift? = null,
    @SerializedName("imageFiles") val imageFiles: List<String>? = null,
    @SerializedName("isActive") val isActive: Boolean? = null
)