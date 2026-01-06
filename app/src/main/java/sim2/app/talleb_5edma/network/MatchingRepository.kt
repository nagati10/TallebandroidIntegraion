package sim2.app.talleb_5edma.network

import io.ktor.client.call.body
import io.ktor.client.request.*
import io.ktor.http.ContentType
import io.ktor.http.contentType
import sim2.app.talleb_5edma.models.*
import sim2.app.talleb_5edma.util.BASE_URL
import sim2.app.talleb_5edma.util.client

/**
 * Repository pour gérer les appels à l'endpoint AI-Matching
 */
class MatchingRepository {

    /**
     * Analyser les disponibilités et préférences de l'utilisateur
     * pour trouver les meilleures opportunités avec l'IA
     * 
     * @param token Token d'authentification JWT
     * @param request Disponibilités et préférences utilisateur
     * @return Liste des matches trouvés avec scores et recommandations
     */
    suspend fun analyzeMatches(
        token: String,
        request: MatchingRequest
    ): MatchingResponse {
        println("CatLog: MatchingRepository - Analyzing matches with AI")
        println("CatLog: Student ID: ${request.studentId}")
        println("CatLog: Disponibilites count: ${request.disponibilites.size}")
        println("CatLog: Preferences: ${request.preferences}")
        
        val response = client.post("$BASE_URL/ai-matching/analyze") {
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer $token")
            setBody(request)
        }
        
        println("CatLog: HTTP Status: ${response.status.value}")
        
        // Vérifier le statut de la réponse
        if (response.status.value !in 200..299) {
            val errorBody = try {
                response.body<String>()
            } catch (e: Exception) {
                "Unable to read error body: ${e.message}"
            }
            println("CatLog: Error response body: $errorBody")
            throw Exception("Failed to analyze matches: HTTP ${response.status.value} - $errorBody")
        }
        
        return try {
            val matchingResponse = response.body<MatchingResponse>()
            println("CatLog: Found ${matchingResponse.matches.size} matches")
            matchingResponse
        } catch (e: Exception) {
            println("CatLog: Error parsing response: ${e.message}")
            // Essayer de parser comme ApiResponse wrapper
            try {
                val apiResponse = response.body<ApiResponse<MatchingResponse>>()
                if (apiResponse.success == true && apiResponse.data != null) {
                    apiResponse.data
                } else {
                    throw Exception(apiResponse.message ?: "Failed to analyze matches")
                }
            } catch (e2: Exception) {
                println("CatLog: Error parsing ApiResponse: ${e2.message}")
                throw Exception("Failed to parse response: ${e.message}")
            }
        }
    }

    /**
     * Analyser avec les disponibilités existantes de l'utilisateur
     * (récupérées automatiquement depuis le backend)
     * 
     * @param token Token d'authentification JWT
     * @param preferences Préférences utilisateur (optionnel)
     * @return Liste des matches trouvés
     */
    suspend fun analyzeWithUserDisponibilites(
        token: String,
        preferences: PreferencesRequest? = null
    ): MatchingResponse {
        println("CatLog: MatchingRepository - Analyzing with user disponibilites from backend")
        
        // 1. Récupérer l'utilisateur courant pour obtenir son ID
        val userRepo = UserRepository()
        val currentUser = userRepo.getCurrentUser(token)
        val studentId = currentUser._id ?: throw Exception("User ID not found")
        
        println("CatLog: Student ID: $studentId")
        
        // 2. Récupérer les disponibilités de l'utilisateur
        val disponibiliteRepo = DisponibiliteRepository()
        val userDisponibilites = disponibiliteRepo.getAllDisponibilites(token)
        
        // 3. Convertir en format attendu par l'API
        val disponibilitesRequest = userDisponibilites.map { dispo ->
            DisponibiliteRequest(
                jour = dispo.jour,
                heureDebut = dispo.heureDebut,
                heureFin = dispo.heureFin
            )
        }
        
        println("CatLog: Using ${disponibilitesRequest.size} disponibilites from user profile")
        
        // 4. Créer la requête et analyser
        val request = MatchingRequest(
            studentId = studentId,
            disponibilites = disponibilitesRequest,
            preferences = preferences
        )
        
        return analyzeMatches(token, request)
    }

    /**
     * Obtenir les recommandations rapides (top 5 matches)
     * 
     * @param token Token d'authentification JWT
     * @param jobType Type de job recherché (optionnel)
     * @return Top 5 des meilleurs matches
     */
    suspend fun getQuickRecommendations(
        token: String,
        jobType: String? = null
    ): List<Match> {
        val preferences = if (jobType != null) {
            PreferencesRequest(jobType = jobType)
        } else {
            null
        }
        
        val response = analyzeWithUserDisponibilites(token, preferences)
        
        // Retourner top 5 triés par score
        return response.matches
            .sortedByDescending { it.scores.score }
            .take(5)
    }
}
