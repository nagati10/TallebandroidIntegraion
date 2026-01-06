package sim2.app.talleb_5edma.network

import android.content.Context
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import sim2.app.talleb_5edma.util.saveEmailLocally
import sim2.app.talleb_5edma.util.saveOTPLocally
import java.util.Locale

// Global test mode variable
var testMode: Boolean = true

private val client = HttpClient()

// Generate a 6-digit OTP
fun generateOTP(): String = (1000..9999).random().toString()

// Get expiry time (15 minutes from now)
fun getExpiryTime(): String {
    val calendar = Calendar.getInstance()
    calendar.add(Calendar.MINUTE, 15)
    val formatter = SimpleDateFormat("hh:mm a", Locale.getDefault())
    return formatter.format(calendar.time)
}

fun sendOTPEmail(
    context: Context,
    scope: CoroutineScope,
    email: String,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    val otp = generateOTP()
    val expiryTime = getExpiryTime()

    println(" CatLog : 🟣 sendOTPEmail: Starting with email: $email")
    println(" CatLog : 🟣 Generated OTP: $otp")
    println(" CatLog : 🟣 Test Mode: $testMode")

    saveOTPLocally(otp, context)
    saveEmailLocally(email, context)

    scope.launch {
        if (testMode) {
            println(" CatLog : 🟡 TEST MODE - Simulating OTP send to $email")
            delay(2000L)
            println(" CatLog : 🟢 TEST MODE - Success (simulated)")
            onSuccess()
        } else {
            sendRealOTPEmail(email, otp, expiryTime, onSuccess, onError)
        }
    }
}

private suspend fun sendRealOTPEmail(
    email: String,
    otp: String,
    expiryTime: String,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    println(" CatLog : 🔵 REAL MODE - Sending actual OTP to $email")

    val jsonBody = """
        {
            "service_id": "service_3gjaqxq",
            "template_id": "template_nl0r1z6", 
            "user_id": "2z8qOtomdqeaLx0uB",
            "template_params": {
                "to_send": "$email",
                "passcode": "$otp",
                "time": "$expiryTime"
            }
        }
    """.trimIndent()

    try {
        val response = client.post("https://api.emailjs.com/api/v1.0/email/send") {
            contentType(ContentType.Application.Json)
            setBody(jsonBody)
        }

        val result: String = response.body()
        println(" CatLog : 🔵 REAL MODE - Response: $result")
        println(" CatLog : 🔵 HTTP Status: ${response.status.value}")

        // Success checking
        val success = result.contains("\"status\":\"success\"") ||
                result.contains("\"status\":200") ||
                result.contains("200") ||
                result.contains("OK") ||
                response.status.value in 200..299

        if (success) {
            println(" CatLog : 🟢 REAL MODE - Email sent successfully")
            onSuccess()
        } else {
            println(" CatLog : 🔴 REAL MODE - Email failed. Response: $result")
            onError("Failed to send OTP. Please try again.")
        }
    } catch (e: Exception) {
        println(" CatLog : 🔴 REAL MODE - Network error: ${e.message}")
        onError("Network error: ${e.message}")
    }
}