package sim2.app.talleb_5edma.models
import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("_id") val _id: String? = null,
    @SerializedName("nom") val nom: String? = null,
    @SerializedName("email") val email: String? = null,
    @SerializedName("contact") val contact: String? = null,
    @SerializedName("role") val role: String? = null,
    @SerializedName("image") val image: String? = null,
    @SerializedName("password") val password: String? = null,  // Change to nullable
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("updatedAt") val updatedAt: String? = null
)