package sim2.app.talleb_5edma.models

import com.google.gson.annotations.SerializedName

data class Disponibilite(
    @SerializedName("_id") val _id: String? = null,
    @SerializedName("userId") val userId: String? = null,
    @SerializedName("jour") val jour: String,
    @SerializedName("heureDebut") val heureDebut: String,
    @SerializedName("heureFin") val heureFin: String,
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("updatedAt") val updatedAt: String? = null
)

data class CreateDisponibiliteRequest(
    @SerializedName("jour") val jour: String,
    @SerializedName("heureDebut") val heureDebut: String,
    @SerializedName("heureFin") val heureFin: String
)

data class UpdateDisponibiliteRequest(
    @SerializedName("jour") val jour: String? = null,
    @SerializedName("heureDebut") val heureDebut: String? = null,
    @SerializedName("heureFin") val heureFin: String? = null
)

