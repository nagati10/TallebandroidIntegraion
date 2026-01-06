package sim2.app.talleb_5edma.network

import okhttp3.MultipartBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import sim2.app.talleb_5edma.models.CvStructuredResponse


interface CvAiApi {

    @Multipart
    @POST("cv-ai/extract-cv")
    suspend fun extractCv(
        @Part file: MultipartBody.Part
    ): CvStructuredResponse
}
