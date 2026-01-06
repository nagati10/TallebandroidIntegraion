package sim2.app.talleb_5edma.network

import com.google.gson.annotations.SerializedName
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.*
import io.ktor.http.ContentType
import io.ktor.http.contentType
import sim2.app.talleb_5edma.models.*
import sim2.app.talleb_5edma.util.BASE_URL
import sim2.app.talleb_5edma.util.client

// Response wrapper for events API
data class EvenementsResponse(
    @SerializedName("data") val data: List<Evenement>? = null,
    @SerializedName("evenements") val evenements: List<Evenement>? = null,
    @SerializedName("events") val events: List<Evenement>? = null
)

class EvenementRepository {

    // ==================== CREATE EVENT ====================
    suspend fun createEvenement(token: String, request: CreateEvenementRequest): Evenement {
        val response = client.post("$BASE_URL/evenements") {
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer $token")
            setBody(request)
        }
        
        // Log request details
        println("CatLog: EvenementRepository - Creating event")
        println("CatLog: Request body - titre: ${request.titre}, type: ${request.type}, date: ${request.date}")
        println("CatLog: Request body - heureDebut: ${request.heureDebut}, heureFin: ${request.heureFin}")
        println("CatLog: HTTP Status: ${response.status.value}")
        
        // Check if response is successful
        if (response.status.value !in 200..299) {
            val errorBody = try {
                response.body<String>()
            } catch (e: Exception) {
                "Unable to read error body: ${e.message}"
            }
            println("CatLog: Error response body: $errorBody")
            throw Exception("Failed to create event: HTTP ${response.status.value} - $errorBody")
        }
        
        return try {
            response.body<Evenement>()
        } catch (e: Exception) {
            println("CatLog: Error parsing response: ${e.message}")
            // Try to parse as ApiResponse
            try {
                val apiResponse = response.body<ApiResponse<Evenement>>()
                if (apiResponse.success == true && apiResponse.data != null) {
                    apiResponse.data
                } else {
                    throw Exception(apiResponse.message ?: "Failed to create event")
                }
            } catch (e2: Exception) {
                println("CatLog: Error parsing ApiResponse: ${e2.message}")
                throw Exception("Failed to parse response: ${e.message}")
            }
        }
    }

    // ==================== GET ALL EVENTS ====================
    suspend fun getAllEvenements(token: String): List<Evenement> {
        val response = client.get("$BASE_URL/evenements") {
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer $token")
        }
        
        return try {
            // Try to parse as array first (direct list)
            response.body<List<Evenement>>()
        } catch (e: Exception) {
            // If it fails, try to parse as ApiResponse wrapper
            try {
                val apiResponse = response.body<ApiResponse<List<Evenement>>>()
                apiResponse.data ?: emptyList()
            } catch (e2: Exception) {
                // If that fails, try other possible structures
                try {
                    val wrapper = response.body<EvenementsResponse>()
                    wrapper.data ?: wrapper.evenements ?: wrapper.events ?: emptyList()
                } catch (e3: Exception) {
                    // If all fail, return empty list and log error
                    println("Error parsing events response: ${e3.message}")
                    emptyList()
                }
            }
        }
    }

    // ==================== GET EVENTS BY DATE RANGE ====================
    suspend fun getEvenementsByDateRange(
        token: String,
        startDate: String,
        endDate: String
    ): List<Evenement> {
        println("CatLog: EvenementRepository - Getting events by date range: $startDate to $endDate")

        val response = client.get("$BASE_URL/evenements/date-range") {
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer $token")
            parameter("startDate", startDate)
            parameter("endDate", endDate)
        }

        println("CatLog: Date range API response status: ${response.status.value}")

        return try {
            // Try to parse as array first (direct list)
            response.body<List<Evenement>>().also {
                println("CatLog: Successfully parsed ${it.size} events from date range API")
            }
        } catch (e: Exception) {
            // If it fails, try to parse as ApiResponse wrapper
            try {
                val apiResponse = response.body<ApiResponse<List<Evenement>>>()
                (apiResponse.data ?: emptyList()).also {
                    println("CatLog: Parsed ${it.size} events from ApiResponse wrapper")
                }
            } catch (e2: Exception) {
                // If that fails, try other possible structures
                try {
                    val wrapper = response.body<EvenementsResponse>()
                    (wrapper.data ?: wrapper.evenements ?: wrapper.events ?: emptyList()).also {
                        println("CatLog: Parsed ${it.size} events from EvenementsResponse wrapper")
                    }
                } catch (e3: Exception) {
                    // If all fail, log error and return empty list
                    println("CatLog: Error parsing date range events response: ${e3.message}")
                    println("CatLog: Raw response body: ${response.body<String>()}")
                    emptyList()
                }
            }
        }
    }

    // ==================== GET EVENTS BY TYPE ====================
    suspend fun getEvenementsByType(token: String, type: String): List<Evenement> {
        return client.get("$BASE_URL/evenements/type/$type") {
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer $token")
        }.body()
    }

    // ==================== GET EVENT BY ID ====================
    suspend fun getEvenementById(token: String, id: String): Evenement {
        return client.get("$BASE_URL/evenements/$id") {
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer $token")
        }.body()
    }

    // ==================== UPDATE EVENT ====================
    suspend fun updateEvenement(
        token: String,
        id: String,
        request: UpdateEvenementRequest
    ): Evenement {
        return client.patch("$BASE_URL/evenements/$id") {
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer $token")
            setBody(request)
        }.body()
    }

    // ==================== DELETE EVENT ====================
    suspend fun deleteEvenement(token: String, id: String): Evenement {
        return client.delete<Evenement>("$BASE_URL/evenements/$id") {
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer $token")
        }
    }
}

