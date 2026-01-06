package sim2.app.talleb_5edma.network

import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

class GoogleAuthHelper(private val context: Context) {

    fun getGoogleSignInClient(): GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("23545175189-u0mfh6g5atlojin01fcjteb7dk79ddd9.apps.googleusercontent.com") // Direct client ID
            .requestEmail()
            .build()

        return GoogleSignIn.getClient(context, gso)
    }

    fun getGoogleToken(data: Intent?): String? {
        return try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account = task.getResult(ApiException::class.java)
            println("CatLog : getGoogleToken: ${account.idToken}")
            account.idToken
        } catch (e: Exception) {
            null
        }
    }
    
    fun signOut() {
        getGoogleSignInClient().signOut()
    }
}