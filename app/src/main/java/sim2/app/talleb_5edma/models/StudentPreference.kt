package sim2.app.talleb_5edma.models

import com.google.gson.annotations.SerializedName

// ==================== ENUMS ====================

enum class StudyLevel {
    @SerializedName("licence_1") LICENCE_1,
    @SerializedName("licence_2") LICENCE_2,
    @SerializedName("licence_3") LICENCE_3,
    @SerializedName("ingénierie") INGENIERIE,
    @SerializedName("mastère") MASTERE,
    @SerializedName("autre") AUTRE
}

enum class StudyDomain {
    @SerializedName("informatique") INFORMATIQUE,
    @SerializedName("infirmier") INFIRMIER,
    @SerializedName("médecine") MEDECINE,
    @SerializedName("mécanique") MECANIQUE,
    @SerializedName("électrique") ELECTRIQUE,
    @SerializedName("autre") AUTRE
}

enum class LookingFor {
    @SerializedName("job") JOB,
    @SerializedName("stage") STAGE,
    @SerializedName("freelance") FREELANCE
}

enum class MainMotivation {
    @SerializedName("argent") ARGENT,
    @SerializedName("experience") EXPERIENCE,
    @SerializedName("enrichissement_cv") ENRICHISSEMENT_CV
}

enum class SoftSkills {
    @SerializedName("communication") COMMUNICATION,
    @SerializedName("organisation") ORGANISATION,
    @SerializedName("sérieux") SERIEUX,
    @SerializedName("adaptabilité") ADAPTABILITE,
    @SerializedName("travail_équipe") TRAVAIL_EQUIPE,
    @SerializedName("leadership") LEADERSHIP,
    @SerializedName("créativité") CREATIVITE,
    @SerializedName("résolution_problèmes") RESOLUTION_PROBLEMES,
    @SerializedName("autre") AUTRE
}

enum class LanguageLevel {
    @SerializedName("débutant") DEBUTANT,
    @SerializedName("intermédiaire") INTERMEDIAIRE,
    @SerializedName("avancé") AVANCE,
    @SerializedName("courant") COURANT
}

enum class Hobbies {
    @SerializedName("sport") SPORT,
    @SerializedName("jeux_video") JEUX_VIDEO,
    @SerializedName("musique") MUSIQUE,
    @SerializedName("design") DESIGN,
    @SerializedName("lecture") LECTURE,
    @SerializedName("voyage") VOYAGE,
    @SerializedName("cuisine") CUISINE,
    @SerializedName("photographie") PHOTOGRAPHIE,
    @SerializedName("autre") AUTRE
}

// ==================== DATA CLASSES ====================

data class BusySlot(
    @SerializedName("time") val time: String,
    @SerializedName("label") val label: String,
    @SerializedName("day") val day: String? = null,
    @SerializedName("_id") val id: String? = null
)

data class AvailabilitySettings(
    @SerializedName("autoSync") val autoSync: Boolean = false,
    @SerializedName("blockAroundJob") val blockAroundJob: Boolean = false
)

data class StudentPreferenceError(
    @SerializedName("field") val field: String? = null,
    @SerializedName("message") val message: String? = null
)

data class StudentPreference(
    @SerializedName("_id") val id: String? = null,
    @SerializedName("userId") val userId: User? = null,
    
    // Step 1: Academic Information
    @SerializedName("study_level") val study_level: StudyLevel? = null,
    @SerializedName("study_domain") val study_domain: StudyDomain? = null,
    
    // Step 2: Search Preferences
    @SerializedName("looking_for") val looking_for: LookingFor? = null,
    @SerializedName("main_motivation") val main_motivation: MainMotivation? = null,
    
    // Step 3: Skills
    @SerializedName("soft_skills") val soft_skills: List<SoftSkills>? = null,
    
    // Step 4: Language Levels
    @SerializedName("langue_arabe") val langue_arabe: LanguageLevel? = null,
    @SerializedName("langue_francais") val langue_francais: LanguageLevel? = null,
    @SerializedName("langue_anglais") val langue_anglais: LanguageLevel? = null,
    
    // Step 5: Hobbies
    @SerializedName("hobbies") val hobbies: List<Hobbies>? = null,
    @SerializedName("has_second_hobby") val has_second_hobby: Boolean? = null,
    
    // Step tracking
    @SerializedName("current_step") val current_step: Int? = null,
    @SerializedName("is_completed") val is_completed: Boolean? = null,
    
    // Availability (disponibilités)
    @SerializedName("busySlots") val busySlots: List<BusySlot>? = null,
    @SerializedName("settings") val settings: AvailabilitySettings? = null,
    
    // Timestamps
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("updatedAt") val updatedAt: String? = null,
    
    // Error handling
    @SerializedName("message") val message: String? = null,
    @SerializedName("statusCode") val statusCode: Int? = null,
    @SerializedName("errors") val errors: List<StudentPreferenceError>? = null
)

// ==================== REQUEST DATA CLASSES ====================

data class CreateStudentPreferenceRequest(
    @SerializedName("study_level") val study_level: StudyLevel,
    @SerializedName("study_domain") val study_domain: StudyDomain,
    @SerializedName("looking_for") val looking_for: LookingFor,
    @SerializedName("main_motivation") val main_motivation: MainMotivation,
    @SerializedName("soft_skills") val soft_skills: List<SoftSkills>,
    @SerializedName("langue_arabe") val langue_arabe: LanguageLevel,
    @SerializedName("langue_francais") val langue_francais: LanguageLevel,
    @SerializedName("langue_anglais") val langue_anglais: LanguageLevel,
    @SerializedName("hobbies") val hobbies: List<Hobbies>,
    @SerializedName("has_second_hobby") val has_second_hobby: Boolean,
    @SerializedName("current_step") val current_step: Int? = null,
    @SerializedName("is_completed") val is_completed: Boolean? = null
)

data class UpdateStudentPreferenceRequest(
    @SerializedName("study_level") val study_level: StudyLevel? = null,
    @SerializedName("study_domain") val study_domain: StudyDomain? = null,
    @SerializedName("looking_for") val looking_for: LookingFor? = null,
    @SerializedName("main_motivation") val main_motivation: MainMotivation? = null,
    @SerializedName("soft_skills") val soft_skills: List<SoftSkills>? = null,
    @SerializedName("langue_arabe") val langue_arabe: LanguageLevel? = null,
    @SerializedName("langue_francais") val langue_francais: LanguageLevel? = null,
    @SerializedName("langue_anglais") val langue_anglais: LanguageLevel? = null,
    @SerializedName("hobbies") val hobbies: List<Hobbies>? = null,
    @SerializedName("has_second_hobby") val has_second_hobby: Boolean? = null,
    @SerializedName("current_step") val current_step: Int? = null,
    @SerializedName("is_completed") val is_completed: Boolean? = null,
    // Availability fields
    @SerializedName("busySlots") val busySlots: List<BusySlot>? = null,
    @SerializedName("settings") val settings: AvailabilitySettings? = null
)

data class UpdateStepRequest(
    @SerializedName("step") val step: Int,
    @SerializedName("data") val data: Map<String, Any>,
    @SerializedName("mark_completed") val mark_completed: Boolean? = null
)

// ==================== RESPONSE WRAPPERS ====================

data class StudentPreferencesResponse(
    @SerializedName("data") val data: List<StudentPreference>? = null,
    @SerializedName("success") val success: Boolean? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("status") val status: Int? = null
)

data class StudentPreferenceResponse(
    @SerializedName("data") val data: StudentPreference? = null,
    @SerializedName("success") val success: Boolean? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("status") val status: Int? = null
)

data class FormProgressResponse(
    @SerializedName("current_step") val current_step: Int? = null,
    @SerializedName("is_completed") val is_completed: Boolean? = null
)

// ==================== STATISTICS DATA CLASSES ====================

data class StudyLevelStats(
    @SerializedName("level") val level: String? = null,
    @SerializedName("count") val count: Int? = null
)

data class StudyDomainStats(
    @SerializedName("domain") val domain: String? = null,
    @SerializedName("count") val count: Int? = null
)

data class LookingForStats(
    @SerializedName("lookingFor") val lookingFor: String? = null,
    @SerializedName("count") val count: Int? = null
)

data class SoftSkillsStats(
    @SerializedName("skill") val skill: String? = null,
    @SerializedName("count") val count: Int? = null
)

data class LanguageStats(
    @SerializedName("level") val level: String? = null,
    @SerializedName("count") val count: Int? = null
)

data class CompletionStats(
    @SerializedName("total_users") val total_users: Int? = null,
    @SerializedName("completed_forms") val completed_forms: Int? = null,
    @SerializedName("completion_rate") val completion_rate: String? = null
)

