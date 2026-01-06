package sim2.app.talleb_5edma.network

import com.google.gson.annotations.SerializedName
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Response model for Nominatim reverse geocoding API
 */
data class NominatimAddress(
    @SerializedName("road") val road: String? = null,
    @SerializedName("house_number") val houseNumber: String? = null,
    @SerializedName("suburb") val suburb: String? = null,
    @SerializedName("city") val city: String? = null,
    @SerializedName("town") val town: String? = null,
    @SerializedName("village") val village: String? = null,
    @SerializedName("state") val state: String? = null,
    @SerializedName("country") val country: String? = null,
    @SerializedName("postcode") val postcode: String? = null
)

data class NominatimResponse(
    @SerializedName("place_id") val placeId: Long? = null,
    @SerializedName("lat") val lat: String? = null,
    @SerializedName("lon") val lon: String? = null,
    @SerializedName("display_name") val displayName: String? = null,
    @SerializedName("address") val address: NominatimAddress? = null
)

/**
 * Parsed location data from reverse geocoding
 */
data class ParsedLocation(
    val address: String,
    val city: String,
    val country: String,
    val latitude: Double,
    val longitude: Double
)

/**
 * Retrofit API interface for Nominatim reverse geocoding
 */
interface NominatimApi {
    @GET("reverse")
    suspend fun reverseGeocode(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("format") format: String = "json",
        @Query("addressdetails") addressDetails: Int = 1
    ): NominatimResponse
}

/**
 * Service for reverse geocoding using OpenStreetMap Nominatim API
 */
class GeocodingService {
    private val api: NominatimApi

    init {
        // Add OkHttp client with User-Agent header (required by Nominatim)
        val okHttpClient = okhttp3.OkHttpClient.Builder()
            .addInterceptor { chain ->
                val original = chain.request()
                val request = original.newBuilder()
                    .header("User-Agent", "TallebKhedma/1.0 (Android App)")
                    .method(original.method, original.body)
                    .build()
                chain.proceed(request)
            }
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://nominatim.openstreetmap.org/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        api = retrofit.create(NominatimApi::class.java)
    }

    /**
     * Performs reverse geocoding to convert GPS coordinates to address information
     * @param latitude GPS latitude
     * @param longitude GPS longitude
     * @return ParsedLocation with address components
     */
    suspend fun reverseGeocode(latitude: Double, longitude: Double): ParsedLocation? {
        return try {
            println("GeocodingService: Requesting geocode for ($latitude, $longitude)")
            val response = api.reverseGeocode(latitude, longitude)
            
            println("GeocodingService: Received response - display_name: ${response.displayName}")
            
            if (response.address == null) {
                println("GeocodingService: No address found for coordinates ($latitude, $longitude)")
                println("GeocodingService: Response display_name was: ${response.displayName}")
                return null
            }

            val addr = response.address
            
            // Build street address from available components
            val streetParts = mutableListOf<String>()
            if (!addr.houseNumber.isNullOrBlank()) {
                streetParts.add(addr.houseNumber)
            }
            if (!addr.road.isNullOrBlank()) {
                streetParts.add(addr.road)
            }
            if (streetParts.isEmpty() && !addr.suburb.isNullOrBlank()) {
                streetParts.add(addr.suburb)
            }
            
            val streetAddress = if (streetParts.isNotEmpty()) {
                streetParts.joinToString(" ")
            } else {
                response.displayName?.split(",")?.firstOrNull()?.trim() ?: "Unknown"
            }

            // Get city name (try different fields)
            val cityName = addr.city 
                ?: addr.town 
                ?: addr.village 
                ?: addr.state 
                ?: "Unknown"

            // Get country name
            val countryName = addr.country ?: "Unknown"

            println("GeocodingService: Geocoded ($latitude, $longitude) -> $streetAddress, $cityName, $countryName")

            ParsedLocation(
                address = streetAddress,
                city = cityName,
                country = countryName,
                latitude = latitude,
                longitude = longitude
            )
        } catch (e: Exception) {
            println("GeocodingService: ERROR - Exception type: ${e.javaClass.simpleName}")
            println("GeocodingService: ERROR - Message: ${e.message}")
            println("GeocodingService: ERROR - Cause: ${e.cause?.message}")
            e.printStackTrace()
            null
        }
    }
}
