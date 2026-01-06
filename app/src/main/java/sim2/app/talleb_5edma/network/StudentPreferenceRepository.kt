package sim2.app.talleb_5edma.network

import com.google.gson.annotations.SerializedName
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import sim2.app.talleb_5edma.models.CreateStudentPreferenceRequest
import sim2.app.talleb_5edma.models.FormProgressResponse
import sim2.app.talleb_5edma.models.LanguageStats
import sim2.app.talleb_5edma.models.LookingForStats
import sim2.app.talleb_5edma.models.SoftSkillsStats
import sim2.app.talleb_5edma.models.StudentPreference
import sim2.app.talleb_5edma.models.StudentPreferenceResponse
import sim2.app.talleb_5edma.models.StudentPreferencesResponse
import sim2.app.talleb_5edma.models.StudyDomainStats
import sim2.app.talleb_5edma.models.StudyLevelStats
import sim2.app.talleb_5edma.models.UpdateStepRequest
import sim2.app.talleb_5edma.models.UpdateStudentPreferenceRequest
import sim2.app.talleb_5edma.util.BASE_URL
import sim2.app.talleb_5edma.util.client

// ==================== RESPONSE DATA CLASSES ====================

data class DeleteStudentPreferenceResponse(
    @SerializedName("message") val message: String? = null
)

data class CompletionStatsResponse(
    @SerializedName("total_users") val total_users: Int? = null,
    @SerializedName("completed_forms") val completed_forms: Int? = null,
    @SerializedName("completion_rate") val completion_rate: String? = null
)

class StudentPreferenceRepository {

    // ==================== CREATE/COMPLETE FORM ====================
    suspend fun createOrCompletePreference(
        token: String,
        createRequest: CreateStudentPreferenceRequest
    ): StudentPreference {
        return client.post("$BASE_URL/student-preferences") {
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer $token")
            setBody(createRequest)
        }.body()
    }

    // ==================== UPDATE STEP ====================
    suspend fun updateStep(
        token: String,
        step: Int,
        data: Map<String, Any>
    ): StudentPreference {
        return client.patch("$BASE_URL/student-preferences/step/$step") {
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer $token")
            setBody(UpdateStepRequest(step = step, data = data))
        }.body()
    }

    // ==================== GET FORM PROGRESS ====================
    suspend fun getFormProgress(token: String): FormProgressResponse {
        return client.get("$BASE_URL/student-preferences/progress") {
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer $token")
        }.body()
    }

    // ==================== GET ALL PREFERENCES (Admin only) ====================
    suspend fun getAllPreferences(token: String): List<StudentPreference> {
        return client.get("$BASE_URL/student-preferences") {
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer $token")
        }.body()
    }

    // ==================== GET MY PREFERENCES ====================
    suspend fun getMyPreferences(token: String): StudentPreference {
        return client.get("$BASE_URL/student-preferences/my-preferences") {
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer $token")
        }.body()
    }

    // ==================== SEARCH BY CRITERIA ====================
    suspend fun searchByCriteria(
        token: String,
        study_level: String? = null,
        study_domain: String? = null,
        looking_for: String? = null,
        main_motivation: String? = null,
        soft_skills: List<String>? = null
    ): List<StudentPreference> {
        return client.get("$BASE_URL/student-preferences/search") {
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer $token")
            
            study_level?.let { parameter("study_level", it) }
            study_domain?.let { parameter("study_domain", it) }
            looking_for?.let { parameter("looking_for", it) }
            main_motivation?.let { parameter("main_motivation", it) }
            soft_skills?.let { 
                it.forEach { skill ->
                    parameter("soft_skills", skill)
                }
            }
        }.body()
    }

    // ==================== GET PREFERENCE BY ID ====================
    suspend fun getPreferenceById(token: String, id: String): StudentPreference {
        return client.get("$BASE_URL/student-preferences/$id") {
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer $token")
        }.body()
    }

    // ==================== UPDATE MY PREFERENCES ====================
    suspend fun updateMyPreferences(
        token: String,
        updateRequest: UpdateStudentPreferenceRequest
    ): StudentPreference {
        return client.patch("$BASE_URL/student-preferences/my-preferences") {
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer $token")
            setBody(updateRequest)
        }.body()
    }

    // ==================== UPDATE PREFERENCE BY ID ====================
    suspend fun updatePreferenceById(
        token: String,
        id: String,
        updateRequest: UpdateStudentPreferenceRequest
    ): StudentPreference {
        return client.patch("$BASE_URL/student-preferences/$id") {
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer $token")
            setBody(updateRequest)
        }.body()
    }

    // ==================== DELETE MY PREFERENCES ====================
    suspend fun deleteMyPreferences(token: String): DeleteStudentPreferenceResponse {
        return client.delete("$BASE_URL/student-preferences/my-preferences") {
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer $token")
        }.body()
    }

    // ==================== DELETE PREFERENCE BY ID ====================
    suspend fun deletePreferenceById(
        token: String,
        id: String
    ): DeleteStudentPreferenceResponse {
        return client.delete("$BASE_URL/student-preferences/$id") {
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer $token")
        }.body()
    }

    // ==================== STATISTICS ENDPOINTS (Admin only) ====================

    // Get statistics by study level
    suspend fun getStudyLevelStats(token: String): List<StudyLevelStats> {
        return client.get("$BASE_URL/student-preferences/stats/study-level") {
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer $token")
        }.body()
    }

    // Get statistics by study domain
    suspend fun getStudyDomainStats(token: String): List<StudyDomainStats> {
        return client.get("$BASE_URL/student-preferences/stats/study-domain") {
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer $token")
        }.body()
    }

    // Get statistics by looking for
    suspend fun getLookingForStats(token: String): List<LookingForStats> {
        return client.get("$BASE_URL/student-preferences/stats/looking-for") {
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer $token")
        }.body()
    }

    // Get completion statistics
    suspend fun getCompletionStats(token: String): CompletionStatsResponse {
        return client.get("$BASE_URL/student-preferences/stats/completion") {
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer $token")
        }.body()
    }

    // Get statistics by soft skills
    suspend fun getSoftSkillsStats(token: String): List<SoftSkillsStats> {
        return client.get("$BASE_URL/student-preferences/stats/soft-skills") {
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer $token")
        }.body()
    }

    // Get statistics by language level
    suspend fun getLanguageStats(
        token: String,
        language: String // "arabe", "francais", or "anglais"
    ): List<LanguageStats> {
        return client.get("$BASE_URL/student-preferences/stats/language/$language") {
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer $token")
        }.body()
    }
}

