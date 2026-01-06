package sim2.app.talleb_5edma.network

import io.ktor.client.call.body
import io.ktor.client.request.*
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import sim2.app.talleb_5edma.models.*
import sim2.app.talleb_5edma.util.BASE_URL
import sim2.app.talleb_5edma.util.client

class ScheduleRepository {

    /**
     * Upload un PDF d'emploi du temps et extraire les cours
     * @param pdfBytes Contenu du fichier PDF en bytes
     * @param fileName Nom du fichier PDF
     * @return Liste des cours extraits
     */
    suspend fun processPdfSchedule(
        pdfBytes: ByteArray,
        fileName: String
    ): ProcessedScheduleResponse {
        println("CatLog: ScheduleRepository - Processing PDF schedule")
        println("CatLog: File name: $fileName, Size: ${pdfBytes.size} bytes")
        
        val response = client.post("$BASE_URL/schedule/process") {
            setBody(MultiPartFormDataContent(
                formData {
                    append("file", pdfBytes, Headers.build {
                        append(HttpHeaders.ContentType, "application/pdf")
                        append(HttpHeaders.ContentDisposition, "filename=\"$fileName\"")
                    })
                }
            ))
        }
        
        println("CatLog: HTTP Status: ${response.status.value}")
        
        if (response.status.value !in 200..299) {
            val errorBody = try {
                response.body<String>()
            } catch (e: Exception) {
                "Unable to read error body: ${e.message}"
            }
            println("CatLog: Error response: $errorBody")
            throw Exception("Failed to process PDF: HTTP ${response.status.value} - $errorBody")
        }
        
        return try {
            response.body<ProcessedScheduleResponse>()
        } catch (e: Exception) {
            println("CatLog: Error parsing response: ${e.message}")
            throw Exception("Failed to parse response: ${e.message}")
        }
    }

    /**
     * Créer automatiquement les événements à partir des cours extraits
     * @param token Token d'authentification
     * @param courses Liste des cours à créer
     * @param weekStartDate Date de début de semaine (optionnel, format: "2024-12-01")
     * @return Réponse avec les événements créés
     */
    suspend fun createEventsFromSchedule(
        token: String,
        courses: List<Course>,
        weekStartDate: String? = null
    ): CreateEventsResponse {
        println("CatLog: ScheduleRepository - Creating events from schedule")
        println("CatLog: Courses count: ${courses.size}, Week start: $weekStartDate")
        
        val request = CreateEventsRequest(
            courses = courses,
            weekStartDate = weekStartDate
        )
        
        val response = client.post("$BASE_URL/schedule/create-events") {
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer $token")
            setBody(request)
        }
        
        println("CatLog: HTTP Status: ${response.status.value}")
        
        if (response.status.value !in 200..299) {
            val errorBody = try {
                response.body<String>()
            } catch (e: Exception) {
                "Unable to read error body: ${e.message}"
            }
            println("CatLog: Error response: $errorBody")
            throw Exception("Failed to create events: HTTP ${response.status.value} - $errorBody")
        }
        
        return try {
            response.body<CreateEventsResponse>()
        } catch (e: Exception) {
            println("CatLog: Error parsing response: ${e.message}")
            // Try with ApiResponse wrapper
            try {
                val apiResponse = response.body<ApiResponse<CreateEventsResponse>>()
                apiResponse.data ?: throw Exception(apiResponse.message ?: "Failed to create events")
            } catch (e2: Exception) {
                println("CatLog: Error parsing ApiResponse: ${e2.message}")
                throw Exception("Failed to parse response: ${e.message}")
            }
        }
    }

    /**
     * Traiter le PDF et créer les événements en une seule opération
     * @param token Token d'authentification
     * @param pdfBytes Contenu du fichier PDF
     * @param fileName Nom du fichier
     * @param weekStartDate Date de début de semaine (optionnel)
     * @return Réponse avec les événements créés
     */
    suspend fun processAndCreateEvents(
        token: String,
        pdfBytes: ByteArray,
        fileName: String,
        weekStartDate: String? = null
    ): CreateEventsResponse {
        // Étape 1: Traiter le PDF
        val processedSchedule = processPdfSchedule(pdfBytes, fileName)
        
        if (processedSchedule.courses.isEmpty()) {
            throw Exception("Aucun cours trouvé dans le PDF")
        }
        
        println("CatLog: Found ${processedSchedule.courses.size} courses in PDF")
        
        // Étape 2: Créer les événements
        return createEventsFromSchedule(token, processedSchedule.courses, weekStartDate)
    }
}
