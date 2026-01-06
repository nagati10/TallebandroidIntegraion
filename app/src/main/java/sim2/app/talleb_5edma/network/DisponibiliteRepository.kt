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

// Response wrapper for disponibilites API
data class DisponibilitesResponse(
    @SerializedName("data") val data: List<Disponibilite>? = null,
    @SerializedName("disponibilites") val disponibilites: List<Disponibilite>? = null,
    @SerializedName("availabilities") val availabilities: List<Disponibilite>? = null
)

class DisponibiliteRepository {

    // ==================== CREATE AVAILABILITY ====================
    suspend fun createDisponibilite(token: String, request: CreateDisponibiliteRequest): Disponibilite {
        val response = client.post("$BASE_URL/disponibilites") {
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer $token")
            setBody(request)
        }
        
        // Log request details
        println("CatLog: DisponibiliteRepository - Creating availability")
        println("CatLog: Request body - jour: ${request.jour}, heureDebut: ${request.heureDebut}, heureFin: ${request.heureFin}")
        println("CatLog: HTTP Status: ${response.status.value}")
        
        // Check if response is successful
        if (response.status.value !in 200..299) {
            val errorBody = try {
                response.body<String>()
            } catch (e: Exception) {
                "Unable to read error body: ${e.message}"
            }
            println("CatLog: Error response body: $errorBody")
            throw Exception("Failed to create availability: HTTP ${response.status.value} - $errorBody")
        }
        
        return try {
            response.body<Disponibilite>()
        } catch (e: Exception) {
            println("CatLog: Error parsing response: ${e.message}")
            // Try to parse as ApiResponse
            try {
                val apiResponse = response.body<ApiResponse<Disponibilite>>()
                if (apiResponse.success == true && apiResponse.data != null) {
                    apiResponse.data
                } else {
                    throw Exception(apiResponse.message ?: "Failed to create availability")
                }
            } catch (e2: Exception) {
                println("CatLog: Error parsing ApiResponse: ${e2.message}")
                throw Exception("Failed to parse response: ${e.message}")
            }
        }
    }

    // ==================== GET ALL AVAILABILITIES ====================
    suspend fun getAllDisponibilites(token: String): List<Disponibilite> {
        val response = client.get("$BASE_URL/disponibilites") {
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer $token")
        }
        
        return try {
            // Try to parse as array first (direct list)
            response.body<List<Disponibilite>>()
        } catch (e: Exception) {
            // If it fails, try to parse as ApiResponse wrapper
            try {
                val apiResponse = response.body<ApiResponse<List<Disponibilite>>>()
                apiResponse.data ?: emptyList()
            } catch (e2: Exception) {
                // If that fails, try other possible structures
                try {
                    val wrapper = response.body<DisponibilitesResponse>()
                    wrapper.data ?: wrapper.disponibilites ?: wrapper.availabilities ?: emptyList()
                } catch (e3: Exception) {
                    // If all fail, return empty list and log error
                    println("Error parsing disponibilites response: ${e3.message}")
                    emptyList()
                }
            }
        }
    }

    // ==================== GET AVAILABILITIES BY DAY ====================
    suspend fun getDisponibilitesByDay(token: String, jour: String): List<Disponibilite> {
        return client.get("$BASE_URL/disponibilites/jour/$jour") {
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer $token")
        }.body()
    }

    // ==================== GET AVAILABILITY BY ID ====================
    suspend fun getDisponibiliteById(token: String, id: String): Disponibilite {
        return client.get("$BASE_URL/disponibilites/$id") {
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer $token")
        }.body()
    }

    // ==================== UPDATE AVAILABILITY ====================
    suspend fun updateDisponibilite(
        token: String,
        id: String,
        request: UpdateDisponibiliteRequest
    ): Disponibilite {
        val response = client.patch("$BASE_URL/disponibilites/$id") {
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer $token")
            setBody(request)
        }
        
        println("CatLog: DisponibiliteRepository - Updating availability $id")
        println("CatLog: HTTP Status: ${response.status.value}")
        
        if (response.status.value !in 200..299) {
            val errorBody = try {
                response.body<String>()
            } catch (e: Exception) {
                "Unable to read error body: ${e.message}"
            }
            println("CatLog: Error response body: $errorBody")
            throw Exception("Failed to update availability: HTTP ${response.status.value} - $errorBody")
        }
        
        return try {
            response.body<Disponibilite>()
        } catch (e: Exception) {
            println("CatLog: Error parsing response: ${e.message}")
            try {
                val apiResponse = response.body<ApiResponse<Disponibilite>>()
                if (apiResponse.success == true && apiResponse.data != null) {
                    apiResponse.data
                } else {
                    throw Exception(apiResponse.message ?: "Failed to update availability")
                }
            } catch (e2: Exception) {
                println("CatLog: Error parsing ApiResponse: ${e2.message}")
                throw Exception("Failed to parse response: ${e.message}")
            }
        }
    }

    // ==================== DELETE AVAILABILITY ====================
    suspend fun deleteDisponibilite(token: String, id: String): Disponibilite {
        return client.delete<Disponibilite>("$BASE_URL/disponibilites/$id") {
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer $token")
        }
    }

    // ==================== DELETE ALL AVAILABILITIES ====================
    suspend fun deleteAllDisponibilites(token: String): String {
        return client.delete<String>("$BASE_URL/disponibilites") {
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer $token")
        }
    }
}

