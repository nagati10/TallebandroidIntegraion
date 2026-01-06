package sim2.app.talleb_5edma.screens

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okio.BufferedSink
import okio.source
import sim2.app.talleb_5edma.models.CvStructuredResponse
import sim2.app.talleb_5edma.network.CvAiRetrofitClient
import sim2.app.talleb_5edma.network.CreateProfileFromCvRequest
import sim2.app.talleb_5edma.network.UserRepository
import sim2.app.talleb_5edma.util.getToken
import kotlin.io.use
import kotlin.text.isEmpty

data class CvUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val result: CvStructuredResponse? = null,
    val selectedFileName: String? = null
)

class CvAiViewModel : ViewModel() {

    var uiState by mutableStateOf(CvUiState())
        private set

    private val userRepository = UserRepository()

    /** 👉 appelé depuis CVAnalysisScreen dans LaunchedEffect(Unit) */
    fun resetState() {
        uiState = CvUiState()
    }

    private fun setState(reducer: CvUiState.() -> CvUiState) {
        uiState = uiState.reducer()
    }

    fun uploadCv(context: Context, uri: Uri) {
        viewModelScope.launch {
            try {
                val fileName = queryFileName(context, uri) ?: "cv.pdf"

                // On lance un nouvel upload → on efface l'ancien résultat
                setState {
                    copy(
                        isLoading = true,
                        error = null,
                        selectedFileName = fileName,
                        result = null
                    )
                }

                val requestBody = object : RequestBody() {
                    override fun contentType() =
                        "application/pdf".toMediaTypeOrNull()

                    override fun writeTo(sink: BufferedSink) {
                        context.contentResolver.openInputStream(uri)?.use { inputStream ->
                            sink.writeAll(inputStream.source())
                        }
                    }
                }

                val multipart = MultipartBody.Part.createFormData(
                    "file",
                    fileName,
                    requestBody
                )

                val api = CvAiRetrofitClient.api
                val response = api.extractCv(multipart)

                setState {
                    copy(
                        isLoading = false,
                        result = response,
                        error = null
                    )
                }

            } catch (e: Exception) {
                e.printStackTrace()
                setState {
                    copy(
                        isLoading = false,
                        error = e.message ?: "Erreur inconnue"
                    )
                }
            }
        }
    }

    /** 👉 Sauvegarder les données extraites du CV dans le profil user (backend Nest) */
    fun saveProfileFromCv(context: Context, cvData: CvStructuredResponse? = null) {
        // Use passed data or fall back to state (for backward compatibility if needed)
        val cv = cvData ?: uiState.result ?: return

        viewModelScope.launch {
            try {
                setState { copy(isLoading = true, error = null) }

                val token = getToken(context)
                if (token.isEmpty()) {
                    setState { copy(isLoading = false, error = "Utilisateur non connecté") }
                    return@launch
                }

                val body = CreateProfileFromCvRequest(
                    name = cv.name,
                    email = cv.email,
                    phone = cv.phone,
                    experience = cv.experience,
                    education = cv.education,
                    skills = cv.skills
                )

                userRepository.updateProfileFromCv(token, body)

                // Pas besoin de changer result, juste arrêter le loading
                setState { copy(isLoading = false) }

            } catch (e: Exception) {
                e.printStackTrace()
                setState {
                    copy(
                        isLoading = false,
                        error = e.message ?: "Erreur lors de la sauvegarde du profil"
                    )
                }
            }
        }
    }

    private fun queryFileName(context: Context, uri: Uri): String? {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (it.moveToFirst() && nameIndex != -1) {
                return it.getString(nameIndex)
            }
        }
        return null
    }
}
