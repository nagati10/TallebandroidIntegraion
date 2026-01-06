package sim2.app.talleb_5edma.network

import sim2.app.talleb_5edma.models.BusySlot
import sim2.app.talleb_5edma.models.AvailabilitySettings
import sim2.app.talleb_5edma.models.StudentPreference
import sim2.app.talleb_5edma.models.UpdateStudentPreferenceRequest

class AvailabilityRepository {
    private val studentPreferenceRepository = StudentPreferenceRepository()

    // ==================== GET MY AVAILABILITY ====================
    suspend fun getMyAvailability(token: String): StudentPreference? {
        return try {
            studentPreferenceRepository.getMyPreferences(token)
        } catch (e: Exception) {
            null // Return null if not found (first time)
        }
    }

    // ==================== SAVE AVAILABILITY (UPSERT) ====================
    // Cette fonction met à jour les disponibilités dans StudentPreference
    suspend fun saveAvailability(
        token: String,
        busySlots: List<BusySlot>,
        settings: AvailabilitySettings
    ): StudentPreference {
        // Récupérer les préférences existantes
        val existing = try {
            studentPreferenceRepository.getMyPreferences(token)
        } catch (e: Exception) {
            null
        }

        // Créer la requête de mise à jour avec les disponibilités
        val updateRequest = UpdateStudentPreferenceRequest(
            // Préserver les champs existants
            study_level = existing?.study_level,
            study_domain = existing?.study_domain,
            looking_for = existing?.looking_for,
            main_motivation = existing?.main_motivation,
            soft_skills = existing?.soft_skills,
            langue_arabe = existing?.langue_arabe,
            langue_francais = existing?.langue_francais,
            langue_anglais = existing?.langue_anglais,
            hobbies = existing?.hobbies,
            has_second_hobby = existing?.has_second_hobby,
            current_step = existing?.current_step,
            is_completed = existing?.is_completed,
            // Mettre à jour les disponibilités
            busySlots = busySlots,
            settings = settings
        )

        // Mettre à jour via StudentPreferenceRepository
        return studentPreferenceRepository.updateMyPreferences(token, updateRequest)
    }
}

