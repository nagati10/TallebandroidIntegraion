package sim2.app.talleb_5edma.models

import com.google.gson.annotations.SerializedName

// Modèle pour les préférences utilisateur
data class UserPreferencesDto(
    @SerializedName("educationLevel") val educationLevel: String? = null,
    @SerializedName("studyField") val studyField: String? = null,
    @SerializedName("searchTypes") val searchTypes: List<String> = emptyList(),
    @SerializedName("mainMotivation") val mainMotivation: String? = null,
    @SerializedName("softSkills") val softSkills: List<String> = emptyList(),
    @SerializedName("languageLevels") val languageLevels: List<String> = emptyList(),
    @SerializedName("interests") val interests: List<String> = emptyList()
)

// Modèle pour la requête d'analyse de routine
data class RoutineInputDataDto(
    @SerializedName("evenements") val evenements: List<EvenementDto>,
    @SerializedName("disponibilites") val disponibilites: List<DisponibiliteDto>,
    @SerializedName("preferences") val preferences: UserPreferencesDto? = null,
    @SerializedName("dateDebut") val dateDebut: String,
    @SerializedName("dateFin") val dateFin: String
)

// Modèle pour la requête d'analyse de routine enhanced (format strict)
data class RoutineInputDataDtoStrict(
    @SerializedName("evenements") val evenements: List<EvenementDtoStrict>,
    @SerializedName("disponibilites") val disponibilites: List<DisponibiliteDtoStrict>,
    @SerializedName("dateDebut") val dateDebut: String,
    @SerializedName("dateFin") val dateFin: String
)

// DTO pour les événements dans la requête (format strict pour API enhanced)
data class EvenementDto(
    @SerializedName("id") val id: String? = null,
    @SerializedName("_id") val _id: String? = null,
    @SerializedName("titre") val titre: String,
    @SerializedName("type") val type: String,
    @SerializedName("date") val date: String,
    @SerializedName("heureDebut") val heureDebut: String,
    @SerializedName("heureFin") val heureFin: String,
    @SerializedName("lieu") val lieu: String? = null,
    @SerializedName("tarifHoraire") val tarifHoraire: Double? = null,
    @SerializedName("couleur") val couleur: String? = null
)

// DTO simplifié pour l'API enhanced (sans champs optionnels null)
data class EvenementDtoStrict(
    @SerializedName("id") val id: String? = null,
    @SerializedName("titre") val titre: String,
    @SerializedName("type") val type: String,
    @SerializedName("date") val date: String,
    @SerializedName("heureDebut") val heureDebut: String,
    @SerializedName("heureFin") val heureFin: String
)

// DTO pour les disponibilités dans la requête
data class DisponibiliteDto(
    @SerializedName("id") val id: String? = null,
    @SerializedName("_id") val _id: String? = null,
    @SerializedName("jour") val jour: String,
    @SerializedName("heureDebut") val heureDebut: String,
    @SerializedName("heureFin") val heureFin: String
)

// DTO simplifié pour l'API enhanced (sans champs optionnels null)
data class DisponibiliteDtoStrict(
    @SerializedName("id") val id: String? = null,
    @SerializedName("jour") val jour: String,
    @SerializedName("heureDebut") val heureDebut: String,
    @SerializedName("heureFin") val heureFin: String
)

// Modèle pour l'analyse hebdomadaire
data class AnalyseHebdomadaire(
    @SerializedName("travail") val travail: CategorieTemps? = null,
    @SerializedName("etudes") val etudes: CategorieTemps? = null,
    @SerializedName("repos") val repos: CategorieTemps? = null,
    @SerializedName("activites") val activites: CategorieTemps? = null
)

// Modèle pour une catégorie de temps
data class CategorieTemps(
    @SerializedName("heures") val heures: Double = 0.0,
    @SerializedName("pourcentage") val pourcentage: Double = 0.0
)

// Modèle pour une recommandation (nouvelle structure)
data class Recommandation(
    @SerializedName("id") val id: String? = null,
    @SerializedName("type") val type: String,
    @SerializedName("titre") val titre: String,
    @SerializedName("description") val description: String,
    @SerializedName("priorite") val priorite: String,
    @SerializedName("actionSuggeree") val actionSuggeree: String? = null
)

// Modèle pour le breakdown du score
data class ScoreBreakdown(
    @SerializedName("baseScore") val baseScore: Int = 100,
    @SerializedName("workStudyBalance") val workStudyBalance: Int = 0,
    @SerializedName("restPenalty") val restPenalty: Int = 0,
    @SerializedName("conflictPenalty") val conflictPenalty: Int = 0,
    @SerializedName("overloadPenalty") val overloadPenalty: Int = 0,
    @SerializedName("bonuses") val bonuses: Int = 0
)

// Modèle pour un conflit
data class Conflict(
    @SerializedName("date") val date: String,
    @SerializedName("event1") val event1: ConflictEvent,
    @SerializedName("event2") val event2: ConflictEvent,
    @SerializedName("severity") val severity: String, // "low", "medium", "high"
    @SerializedName("suggestion") val suggestion: String,
    @SerializedName("overlapDuration") val overlapDuration: Int // en minutes
)

data class ConflictEvent(
    @SerializedName("titre") val titre: String,
    @SerializedName("heureDebut") val heureDebut: String
)

// Modèle pour un jour surchargé
data class OverloadedDay(
    @SerializedName("date") val date: String,
    @SerializedName("jour") val jour: String,
    @SerializedName("totalHours") val totalHours: Double,
    @SerializedName("level") val level: String, // "élevé", "très élevé", "critique"
    @SerializedName("recommendations") val recommendations: List<String> = emptyList()
)

// Modèle pour un créneau disponible
data class AvailableTimeSlot(
    @SerializedName("jour") val jour: String,
    @SerializedName("heureDebut") val heureDebut: String,
    @SerializedName("heureFin") val heureFin: String,
    @SerializedName("duration") val duration: Double // en heures
)

// Modèle pour l'analyse hebdomadaire (nouvelle structure)
data class AnalyseHebdomadaireEnhanced(
    @SerializedName("heuresTravail") val heuresTravail: Double = 0.0,
    @SerializedName("heuresEtudes") val heuresEtudes: Double = 0.0,
    @SerializedName("heuresRepos") val heuresRepos: Double = 0.0,
    @SerializedName("heuresActivites") val heuresActivites: Double = 0.0
)

// Modèle pour le résumé de santé
data class HealthSummary(
    @SerializedName("status") val status: String, // "excellent", "bon", "moyen", "critique"
    @SerializedName("mainIssues") val mainIssues: List<String> = emptyList(),
    @SerializedName("mainStrengths") val mainStrengths: List<String> = emptyList()
)

// Modèle pour les données de la réponse enhanced
data class RoutineAnalysisDataEnhanced(
    @SerializedName("scoreEquilibre") val scoreEquilibre: Int,
    @SerializedName("scoreBreakdown") val scoreBreakdown: ScoreBreakdown? = null,
    @SerializedName("conflicts") val conflicts: List<Conflict> = emptyList(),
    @SerializedName("overloadedDays") val overloadedDays: List<OverloadedDay> = emptyList(),
    @SerializedName("availableTimeSlots") val availableTimeSlots: List<AvailableTimeSlot> = emptyList(),
    @SerializedName("recommandations") val recommandations: List<Recommandation> = emptyList(),
    @SerializedName("analyseHebdomadaire") val analyseHebdomadaire: AnalyseHebdomadaireEnhanced? = null,
    @SerializedName("healthSummary") val healthSummary: HealthSummary? = null
)

// Modèle pour la réponse complète de l'API enhanced
data class RoutineAnalysisResponseEnhanced(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: RoutineAnalysisDataEnhanced? = null,
    @SerializedName("message") val message: String? = null
)

// ====== ANCIENS MODÈLES (pour compatibilité) ======

// Modèle pour une recommandation (ancienne structure - gardé pour compatibilité)
data class RecommandationOld(
    @SerializedName("titre") val titre: String,
    @SerializedName("description") val description: String,
    @SerializedName("categorie") val categorie: String,
    @SerializedName("priorite") val priorite: String,
    @SerializedName("action") val action: String? = null
)

// Modèle pour les données de la réponse (ancienne structure)
data class RoutineAnalysisData(
    @SerializedName("id") val id: String? = null,
    @SerializedName("_id") val _id: String? = null,
    @SerializedName("dateAnalyse") val dateAnalyse: String,
    @SerializedName("scoreEquilibre") val scoreEquilibre: Int,
    @SerializedName("analyseHebdomadaire") val analyseHebdomadaire: AnalyseHebdomadaire? = null,
    @SerializedName("recommandations") val recommandations: List<RecommandationOld> = emptyList()
)

// Modèle pour la réponse complète de l'API (ancienne structure)
data class RoutineAnalysisResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: RoutineAnalysisData? = null,
    @SerializedName("message") val message: String? = null
)

