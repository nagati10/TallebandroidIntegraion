package sim2.app.talleb_5edma.models

import com.google.gson.annotations.SerializedName

/**
 * Modèle pour une disponibilité à envoyer au backend
 */
data class DisponibiliteRequest(
    @SerializedName("jour") val jour: String,           // "Lundi", "Mardi", etc.
    @SerializedName("heureDebut") val heureDebut: String, // "09:00"
    @SerializedName("heureFin") val heureFin: String      // "17:00"
)

/**
 * Modèle pour les préférences utilisateur
 */
data class PreferencesRequest(
    @SerializedName("jobType") val jobType: String? = null,        // "stage", "cdi", "freelance"
    @SerializedName("sector") val sector: String? = null,          // "tech", "marketing", etc.
    @SerializedName("location") val location: String? = null,      // "Paris", "Lyon", etc.
    @SerializedName("minSalary") val minSalary: Double? = null,    // Salaire minimum
    @SerializedName("maxDistance") val maxDistance: Int? = null    // Distance max en km
)

/**
 * Requête complète pour l'analyse AI-Matching
 */
data class MatchingRequest(
    @SerializedName("studentId") val studentId: String,
    @SerializedName("disponibilites") val disponibilites: List<DisponibiliteRequest>,
    @SerializedName("preferences") val preferences: PreferencesRequest? = null
)

/**
 * Scores de compatibilité pour un match
 */
data class MatchScores(
    @SerializedName("score") val score: Int,                      // Score global (0-100)
    @SerializedName("scheduleScore") val scheduleScore: Int? = null,    // Compatibilité horaires
    @SerializedName("skillsScore") val skillsScore: Int? = null,        // Compatibilité compétences
    @SerializedName("locationScore") val locationScore: Int? = null     // Compatibilité localisation
)

/**
 * Un match (opportunité) trouvé par l'IA
 */
data class Match(
    @SerializedName("_id") val _id: String? = null,
    @SerializedName("titre") val titre: String,
    @SerializedName("description") val description: String? = null,
    @SerializedName("entreprise") val entreprise: String? = null,
    @SerializedName("type") val type: String? = null,              // "stage", "cdi", "freelance"
    @SerializedName("location") val location: String? = null,
    @SerializedName("salaire") val salaire: String? = null,
    @SerializedName("duree") val duree: String? = null,
    @SerializedName("scores") val scores: MatchScores,
    @SerializedName("recommendation") val recommendation: String,
    @SerializedName("matchReasons") val matchReasons: List<String>? = null  // Raisons du match
)

/**
 * Réponse complète de l'analyse AI-Matching
 */
data class MatchingResponse(
    @SerializedName("success") val success: Boolean? = true,
    @SerializedName("message") val message: String? = null,
    @SerializedName("matches") val matches: List<Match>,
    @SerializedName("totalMatches") val totalMatches: Int? = null,
    @SerializedName("analysisTime") val analysisTime: String? = null  // Temps d'analyse
)
