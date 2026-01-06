package sim2.app.talleb_5edma.models

data class CvStructuredResponse(
    val name: String?,
    val email: String?,
    val phone: String?,
    val experience: List<String> = emptyList(),
    val education: List<String> = emptyList(),
    val skills: List<String> = emptyList()
)
