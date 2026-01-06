package sim2.app.talleb_5edma.util
import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import sim2.app.talleb_5edma.network.WebSocketCallManager

const val PREF_NAME = "user"
const val KEY_EMAIL = "email"
const val KEY_PASSWORD = "password"
const val TOKEN_KEY = "access_token"

const val APP_PREFS_NAME = "app_prefs"
const val SAVED_OTP_KEY = "saved_otp"
const val USER_EMAIL_KEY = "user_email"

fun getSharedPref(context: Context): SharedPreferences {
    return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
}

fun getAppPrefs(context: Context): SharedPreferences {
    return context.getSharedPreferences(APP_PREFS_NAME, Context.MODE_PRIVATE)
}

fun saveUserData(context: Context, email: String, password: String, token: String) {
    getSharedPref(context).edit {
        putString(KEY_EMAIL, email)
        putString(KEY_PASSWORD, password)
        putString(TOKEN_KEY, token)
        apply()
    }
}

// Add function to force token refresh
fun refreshToken(context: Context): String {
    val token = getToken(context)
    println("CatLog: Token refresh - '${token.take(10)}...' (${token.length} chars)")
    return token
}
fun clearUserData(context: Context) {
    getSharedPref(context).edit {
        clear()
        apply()
    }
}

fun getEmail(context: Context): String {
    return getSharedPref(context).getString(KEY_EMAIL, "").orEmpty()
}

fun getToken(context: Context): String {
    return getSharedPref(context).getString(TOKEN_KEY, "").orEmpty()
}

// Save OTP to SharedPreferences
fun saveOTPLocally(otp: String, context: Context) {
    getAppPrefs(context).edit {
        putString(SAVED_OTP_KEY, otp)
        apply()
    }
}

// Get saved OTP from SharedPreferences
fun getSavedOTP(context: Context): String {
    return getAppPrefs(context).getString(SAVED_OTP_KEY, "") ?: ""
}

// Save email for later use
fun saveEmailLocally(email: String, context: Context) {
    getAppPrefs(context).edit {
        putString(USER_EMAIL_KEY, email)
        apply()
    }
}

// Get saved email
fun getSavedEmail(context: Context): String {
    return getAppPrefs(context).getString(USER_EMAIL_KEY, "") ?: ""
}

// Add this function to help debug token issues
fun debugTokenInfo(context: Context): String {
    val token = getToken(context)
    val email = getEmail(context)
    val savedEmail = getSavedEmail(context)
    val savedOTP = getSavedOTP(context)

    return "MainPrefs - Token: ${token.length} chars, Email: $email | " +
            "AppPrefs - SavedEmail: $savedEmail, SavedOTP: $savedOTP"
}

// FIXED: Proper forceClearAllData function that clears EVERYTHING
fun forceClearAllData(context: Context, callManager: WebSocketCallManager? = null ) {
    // Clear main user preferences (PREF_NAME = "user")
    getSharedPref(context).edit {
        clear()
        apply()
    }

    // Clear app_prefs (APP_PREFS_NAME = "app_prefs")
    getAppPrefs(context).edit {
        clear()
        apply()
    }
    callManager?.disconnect()
    // Debug: Verify everything is cleared
    val mainPrefsCleared = getToken(context).isEmpty() && getEmail(context).isEmpty()
    val appPrefsCleared = getSavedOTP(context).isEmpty() && getSavedEmail(context).isEmpty()

    verifyDataCleared(context)
}

// Enhanced function to check if user is logged in
fun isUserLoggedIn(context: Context): Boolean {
    return getToken(context).isNotEmpty()
}

// Function to get complete user info for debugging
fun getUserInfoForDebug(context: Context): String {
    return "MainPrefs - Token: '${getToken(context)}' (${getToken(context).length} chars), " +
            "Email: '${getEmail(context)}' | " +
            "AppPrefs - SavedEmail: '${getSavedEmail(context)}', " +
            "SavedOTP: '${getSavedOTP(context)}' | " +
            "LoggedIn: ${isUserLoggedIn(context)}"
}

// NEW: Function to clear only specific app prefs (optional)
fun clearAppPrefsData(context: Context) {
    getAppPrefs(context).edit {
        remove(SAVED_OTP_KEY)
        remove(USER_EMAIL_KEY)
        apply()
    }
    println("CatLog: App prefs data cleared (OTP and saved email)")
}

// Add this function to verify data is properly cleared
fun verifyDataCleared(context: Context): Boolean {
    val tokenCleared = getToken(context).isEmpty()
    val emailCleared = getEmail(context).isEmpty()
    val savedEmailCleared = getSavedEmail(context).isEmpty()
    val savedOTPCleared = getSavedOTP(context).isEmpty()

    val allCleared = tokenCleared && emailCleared && savedEmailCleared && savedOTPCleared

    println("CatLog: Data cleared verification - " +
            "Token: $tokenCleared, " +
            "Email: $emailCleared, " +
            "SavedEmail: $savedEmailCleared, " +
            "SavedOTP: $savedOTPCleared, " +
            "AllCleared: $allCleared")

    return allCleared
}

// ==================== ROUTINE ANALYSIS CACHE ====================

const val ROUTINE_CACHE_PREFS = "routine_cache"
const val ROUTINE_CACHE_KEY = "routine_analysis_data"
const val ROUTINE_CACHE_TIMESTAMP_KEY = "routine_analysis_timestamp"
const val CACHE_DURATION_MS = 5 * 60 * 1000L // 5 minutes

fun getRoutineCachePrefs(context: Context): SharedPreferences {
    return context.getSharedPreferences(ROUTINE_CACHE_PREFS, Context.MODE_PRIVATE)
}

fun saveRoutineAnalysisCache(context: Context, data: String) {
    getRoutineCachePrefs(context).edit {
        putString(ROUTINE_CACHE_KEY, data)
        putLong(ROUTINE_CACHE_TIMESTAMP_KEY, System.currentTimeMillis())
        apply()
    }
    println("CatLog: Routine analysis cache saved")
}

fun getRoutineAnalysisCache(context: Context): String? {
    val prefs = getRoutineCachePrefs(context)
    val timestamp = prefs.getLong(ROUTINE_CACHE_TIMESTAMP_KEY, 0)
    val now = System.currentTimeMillis()
    
    if (now - timestamp > CACHE_DURATION_MS) {
        println("CatLog: Routine analysis cache expired")
        clearRoutineAnalysisCache(context)
        return null
    }
    
    val cached = prefs.getString(ROUTINE_CACHE_KEY, null)
    if (cached != null) {
        println("CatLog: Routine analysis cache found (age: ${(now - timestamp) / 1000}s)")
    }
    return cached
}

fun clearRoutineAnalysisCache(context: Context) {
    getRoutineCachePrefs(context).edit {
        clear()
        apply()
    }
    println("CatLog: Routine analysis cache cleared")
}

fun isRoutineCacheValid(context: Context): Boolean {
    val prefs = getRoutineCachePrefs(context)
    val timestamp = prefs.getLong(ROUTINE_CACHE_TIMESTAMP_KEY, 0)
    val now = System.currentTimeMillis()
    return (now - timestamp) <= CACHE_DURATION_MS && prefs.getString(ROUTINE_CACHE_KEY, null) != null
}