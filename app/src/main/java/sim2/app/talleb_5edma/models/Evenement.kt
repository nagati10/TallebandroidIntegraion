package sim2.app.talleb_5edma.models

import com.google.gson.annotations.SerializedName

data class Evenement(
    @SerializedName("_id") val _id: String? = null,
    @SerializedName("userId") val userId: String? = null,
    @SerializedName("titre") val titre: String,
    @SerializedName("type") val type: String,
    @SerializedName("date") val date: String,
    @SerializedName("heureDebut") val heureDebut: String,
    @SerializedName("heureFin") val heureFin: String,
    @SerializedName("lieu") val lieu: String? = null,
    @SerializedName("tarifHoraire") val tarifHoraire: Double? = null,
    @SerializedName("couleur") val couleur: String? = null,
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("updatedAt") val updatedAt: String? = null
)

data class CreateEvenementRequest(
    @SerializedName("titre") val titre: String,
    @SerializedName("type") val type: String,
    @SerializedName("date") val date: String,
    @SerializedName("heureDebut") val heureDebut: String,
    @SerializedName("heureFin") val heureFin: String,
    @SerializedName("lieu") val lieu: String? = null,
    @SerializedName("tarifHoraire") val tarifHoraire: Double? = null,
    @SerializedName("couleur") val couleur: String? = null
)

data class UpdateEvenementRequest(
    @SerializedName("titre") val titre: String? = null,
    @SerializedName("type") val type: String? = null,
    @SerializedName("date") val date: String? = null,
    @SerializedName("heureDebut") val heureDebut: String? = null,
    @SerializedName("heureFin") val heureFin: String? = null,
    @SerializedName("lieu") val lieu: String? = null,
    @SerializedName("tarifHoraire") val tarifHoraire: Double? = null,
    @SerializedName("couleur") val couleur: String? = null
)

