package sim2.app.talleb_5edma.network

import com.google.gson.annotations.SerializedName
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import sim2.app.talleb_5edma.models.CreateOffreRequest
import sim2.app.talleb_5edma.models.LikeResponse
import sim2.app.talleb_5edma.models.Offre
import sim2.app.talleb_5edma.models.Offre2
import sim2.app.talleb_5edma.models.UpdateOffreRequest
import sim2.app.talleb_5edma.util.BASE_URL
import sim2.app.talleb_5edma.util.client

// ==================== RESPONSE DATA CLASSES ====================

data class DeleteOffreResponse(
    @SerializedName("message") val message: String? = null
)

class OffreRepository {

    // ==================== CREATE OFFER (UPDATED FOR MULTIPART) ====================
    suspend fun createOffre(
        token: String,
        createOffreRequest: CreateOffreRequest,
        imageFiles: List<Pair<ByteArray, String>> = emptyList()
    ): Offre2 {
        return client.post("$BASE_URL/offre") {
            header("Authorization", "Bearer $token")

            setBody(MultiPartFormDataContent(
                formData {
                    // Append all text fields from CreateOffreRequest
                    append("title", createOffreRequest.title)
                    append("description", createOffreRequest.description)
                    append("company", createOffreRequest.company)

                    // Append optional fields if they exist
                    createOffreRequest.tags?.let { tags ->
                        append("tags", tags.joinToString(","))
                    }

                    createOffreRequest.exigences?.let { exigences ->
                        append("exigences", exigences.joinToString(","))
                    }

                    createOffreRequest.category?.let { category ->
                        append("category", category)
                    }

                    createOffreRequest.salary?.let { salary ->
                        append("salary", salary)
                    }

                    createOffreRequest.expiresAt?.let { expiresAt ->
                        append("expiresAt", expiresAt)
                    }

                    createOffreRequest.jobType?.let { jobType ->
                        append("jobType", jobType.name)
                    }

                    createOffreRequest.shift?.let { shift ->
                        append("shift", shift.name)
                    }

                    createOffreRequest.isActive?.let { isActive ->
                        append("isActive", isActive.toString())
                    }

                    createOffreRequest.reference?.let { reference ->
                        append("reference", reference)
                    }

                    // Append location as JSON string with coordinates
                    val coordinatesJson = if (createOffreRequest.location.coordinates != null) {
                        """"lat": ${createOffreRequest.location.coordinates.lat}, "lng": ${createOffreRequest.location.coordinates.lng}"""
                    } else {
                        null
                    }
                    
                    val locationJson = if (coordinatesJson != null) {
                        """{
                        "address": "${createOffreRequest.location.address}",
                        "city": "${createOffreRequest.location.city}",
                        "country": "${createOffreRequest.location.country}",
                        "coordinates": {$coordinatesJson}
                    }""".trimIndent()
                    } else {
                        """{
                        "address": "${createOffreRequest.location.address}",
                        "city": "${createOffreRequest.location.city}",
                        "country": "${createOffreRequest.location.country}",
                        "coordinates": null
                    }""".trimIndent()
                    }
                    
                    println("OffreRepository: Sending location with coordinates: lat=${createOffreRequest.location.coordinates?.lat}, lng=${createOffreRequest.location.coordinates?.lng}")
                    append("location", locationJson)

                    // Append image files
                    imageFiles.forEachIndexed { index, (imageBytes, fileName) ->
                        append(
                            "imageFiles",
                            imageBytes,
                            Headers.build {
                                append(HttpHeaders.ContentType, "image/jpeg")
                                append(HttpHeaders.ContentDisposition, "filename=\"$fileName\"")
                            }
                        )
                    }
                }
            ))
        }.body()
    }

    // ==================== GET ALL ACTIVE OFFERS ====================
    suspend fun getAllActiveOffers(): List<Offre> {
        return client.get("$BASE_URL/offre") {
            contentType(ContentType.Application.Json)
        }.body()
    }

    // ==================== SEARCH OFFERS ====================
    suspend fun searchOffers(query: String): List<Offre> {
        return client.get("$BASE_URL/offre/search") {
            contentType(ContentType.Application.Json)
            parameter("q", query)
        }.body()
    }

    // ==================== GET OFFERS BY TAGS ====================
    suspend fun getOffersByTags(tags: List<String>): List<Offre> {
        return client.get("$BASE_URL/offre/tags") {
            contentType(ContentType.Application.Json)
            parameter("tags", tags.joinToString(","))
        }.body()
    }

    // ==================== GET OFFERS BY CITY ====================
    suspend fun getOffersByCity(city: String): List<Offre> {
        return client.get("$BASE_URL/offre/location/$city") {
            contentType(ContentType.Application.Json)
        }.body()
    }

    // ==================== GET USER'S OFFERS ====================
    suspend fun getMyOffers(token: String): List<Offre> {
        return client.get("$BASE_URL/offre/my-offers") {
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer $token")
        }.body()
    }

    // ==================== GET LIKED OFFERS ====================
    suspend fun getLikedOffers(token: String): List<Offre> {
        return client.get("$BASE_URL/offre/liked") {
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer $token")
        }.body()
    }

    // ==================== GET OFFERS BY USER ID ====================
    suspend fun getOffersByUserId(userId: String): List<Offre> {
        return client.get("$BASE_URL/offre/user/$userId") {
            contentType(ContentType.Application.Json)
        }.body()
    }

    // ==================== GET POPULAR OFFERS ====================
    suspend fun getPopularOffers(): List<Offre> {
        return client.get("$BASE_URL/offre/popular") {
            contentType(ContentType.Application.Json)
        }.body()
    }

    // ==================== GET OFFER BY ID ====================
    suspend fun getOffreById(id: String): Offre {
        return client.get("$BASE_URL/offre/$id") {
            contentType(ContentType.Application.Json)
        }.body()
    }

    // ==================== UPDATE OFFER ====================
    suspend fun updateOffre(
        token: String,
        id: String,
        updateOffreRequest: UpdateOffreRequest
    ): Offre {
        return client.patch("$BASE_URL/offre/$id") {
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer $token")
            setBody(updateOffreRequest)
        }.body()
    }

    // ==================== DELETE OFFER ====================
    suspend fun deleteOffre(token: String, id: String): DeleteOffreResponse {
        return client.delete("$BASE_URL/offre/$id") {
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer $token")
        }.body()
    }

    // ==================== LIKE/UNLIKE OFFER ====================
    suspend fun toggleLikeOffre(token: String, id: String): LikeResponse {
        return client.post("$BASE_URL/offre/$id/like") {
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer $token")
        }.body()
    }

    // ==================== HELPER METHODS ====================

    // Check if current user has liked an offer
    suspend fun hasUserLikedOffre(token: String, offreId: String): Boolean {
        return try {
            val likedOffers = getLikedOffers(token)
            likedOffers.any { it.id == offreId }
        } catch (e: Exception) {
            false
        }
    }
}