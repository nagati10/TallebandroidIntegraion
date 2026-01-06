package sim2.app.talleb_5edma.network



import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import sim2.app.talleb_5edma.util.BASE_URL

object CvAiRetrofitClient {
    private val client = OkHttpClient.Builder()
        .connectTimeout(300, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(300, java.util.concurrent.TimeUnit.SECONDS)
        .writeTimeout(300, java.util.concurrent.TimeUnit.SECONDS)
        .build()

    val api: CvAiApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)

            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CvAiApi::class.java)
    }
}
