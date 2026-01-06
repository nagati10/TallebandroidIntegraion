package sim2.app.talleb_5edma.models

import com.google.gson.annotations.SerializedName

/**
 * Modèle pour un cours extrait du PDF
 */
data class Course(
    @SerializedName("day") val day: String,           // "Monday", "Tuesday", etc.
    @SerializedName("start") val start: String,       // "09:00"
    @SerializedName("end") val end: String,           // "10:30"
    @SerializedName("subject") val subject: String,   // "Mathématiques"
    @SerializedName("classroom") val classroom: String? = null, // "G102"
    @SerializedName("professor") val professor: String? = null  // Optionnel
)

/**
 * Réponse du backend après traitement du PDF
 */
data class ProcessedScheduleResponse(
    @SerializedName("success") val success: Boolean? = true,
    @SerializedName("courses") val courses: List<Course>,
    @SerializedName("message") val message: String? = null
)

/**
 * Requête pour créer les événements automatiquement
 */
data class CreateEventsRequest(
    @SerializedName("courses") val courses: List<Course>,
    @SerializedName("weekStartDate") val weekStartDate: String? = null // "2024-12-01" (optionnel)
)

/**
 * Réponse après création des événements
 */
data class CreateEventsResponse(
    @SerializedName("success") val success: Boolean? = true,
    @SerializedName("message") val message: String,
    @SerializedName("eventsCreated") val eventsCreated: Int,
    @SerializedName("events") val events: List<Evenement>? = null
)
