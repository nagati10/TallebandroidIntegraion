package sim2.app.talleb_5edma.network

import io.ktor.client.call.body
import io.ktor.client.request.*
import io.ktor.http.ContentType
import io.ktor.http.contentType
import sim2.app.talleb_5edma.models.*
import sim2.app.talleb_5edma.util.BASE_URL
import sim2.app.talleb_5edma.util.client

class RoutineRepository {

    /**
     * Analyse la routine hebdomadaire avec IA (nouvelle version enhanced)
     * @param token Token d'authentification
     * @param request Données de la routine à analyser (format strict)
     * @return Réponse contenant l'analyse de la routine enhanced
     */
    suspend fun analyzeRoutineEnhanced(
        token: String,
        request: RoutineInputDataDtoStrict
    ): RoutineAnalysisResponseEnhanced {
        println("CatLog: RoutineRepository - Analyzing routine (enhanced)")
        println("CatLog: Request - dateDebut: ${request.dateDebut}, dateFin: ${request.dateFin}")
        println("CatLog: Request - evenements count: ${request.evenements.size}, disponibilites count: ${request.disponibilites.size}")
        
        // Log détaillé de la requête
        if (request.evenements.isNotEmpty()) {
            println("CatLog: Sample event - ${request.evenements.first()}")
        }
        if (request.disponibilites.isNotEmpty()) {
            println("CatLog: Sample disponibilite - ${request.disponibilites.first()}")
        }
        
        val response = client.post("$BASE_URL/ai/routine/analyze-enhanced") {
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer $token")
            setBody(request)
        }

        println("CatLog: HTTP Status: ${response.status.value}")

        // Check if response is successful
        if (response.status.value !in 200..299) {
            val errorBody = try {
                response.body<String>()
            } catch (e: Exception) {
                "Unable to read error body: ${e.message}"
            }
            println("CatLog: Error response body: $errorBody")
            throw Exception("Failed to analyze routine: HTTP ${response.status.value} - $errorBody")
        }

        return try {
            val enhancedResponse = response.body<RoutineAnalysisResponseEnhanced>()
            println("CatLog: Analysis successful - Score: ${enhancedResponse.data?.scoreEquilibre}, Conflicts: ${enhancedResponse.data?.conflicts?.size}, Overloaded days: ${enhancedResponse.data?.overloadedDays?.size}")
            enhancedResponse
        } catch (e: Exception) {
            println("CatLog: Error parsing enhanced response: ${e.message}")
            // Try to parse as ApiResponse wrapper
            try {
                val apiResponse = response.body<ApiResponse<RoutineAnalysisDataEnhanced>>()
                if (apiResponse.success == true && apiResponse.data != null) {
                    RoutineAnalysisResponseEnhanced(
                        success = true,
                        data = apiResponse.data,
                        message = apiResponse.message
                    )
                } else {
                    throw Exception(apiResponse.message ?: "Failed to analyze routine")
                }
            } catch (e2: Exception) {
                println("CatLog: Error parsing ApiResponse: ${e2.message}")
                throw Exception("Failed to parse response: ${e.message}")
            }
        }
    }

    /**
     * Analyse la routine hebdomadaire avec IA (ancienne version - gardée pour compatibilité)
     * @param token Token d'authentification
     * @param request Données de la routine à analyser
     * @return Réponse contenant l'analyse de la routine
     */
    suspend fun analyzeRoutine(
        token: String,
        request: RoutineInputDataDto
    ): RoutineAnalysisResponse {
        val response = client.post("$BASE_URL/ai/routine/analyze") {
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer $token")
            setBody(request)
        }

        // Log request details
        println("CatLog: RoutineRepository - Analyzing routine (legacy)")
        println("CatLog: Request - dateDebut: ${request.dateDebut}, dateFin: ${request.dateFin}")
        println("CatLog: Request - evenements count: ${request.evenements.size}, disponibilites count: ${request.disponibilites.size}")
        println("CatLog: HTTP Status: ${response.status.value}")

        // Check if response is successful
        if (response.status.value !in 200..299) {
            val errorBody = try {
                response.body<String>()
            } catch (e: Exception) {
                "Unable to read error body: ${e.message}"
            }
            println("CatLog: Error response body: $errorBody")
            throw Exception("Failed to analyze routine: HTTP ${response.status.value} - $errorBody")
        }

        return try {
            response.body<RoutineAnalysisResponse>()
        } catch (e: Exception) {
            println("CatLog: Error parsing response: ${e.message}")
            // Try to parse as ApiResponse wrapper
            try {
                val apiResponse = response.body<ApiResponse<RoutineAnalysisData>>()
                if (apiResponse.success == true && apiResponse.data != null) {
                    RoutineAnalysisResponse(
                        success = true,
                        data = apiResponse.data,
                        message = apiResponse.message
                    )
                } else {
                    throw Exception(apiResponse.message ?: "Failed to analyze routine")
                }
            } catch (e2: Exception) {
                println("CatLog: Error parsing ApiResponse: ${e2.message}")
                throw Exception("Failed to parse response: ${e.message}")
            }
        }
    }
}

