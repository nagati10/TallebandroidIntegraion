// ==================== PACKAGE DECLARATION ====================
// This tells the computer where this file belongs in our app's organization
// Think of it like putting a file in the "network" folder where we keep internet-related tools
package sim2.app.talleb_5edma.network

import com.google.gson.annotations.SerializedName

// ==================== API RESPONSE DATA CLASS ====================
// This is like a standardized envelope that wraps all responses from our server
// "<T>" means this can hold any type of data - it's like a flexible container
// Think of it like a package that can hold books, clothes, or electronics
data class ApiResponse<T>(
    @SerializedName("success") val success: Boolean?,
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: T?,
    @SerializedName("status") val status: Int?
)