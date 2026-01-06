package sim2.app.talleb_5edma.network

data class CreateProfileFromCvRequest(
    val name: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val experience: List<String>? = null,
    val education: List<String>? = null,
    val skills: List<String>? = null
)
