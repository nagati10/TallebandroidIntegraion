package sim2.app.talleb_5edma.screens

import android.util.Patterns
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import sim2.app.talleb_5edma.R
import sim2.app.talleb_5edma.Routes
import sim2.app.talleb_5edma.network.UserRepository
import sim2.app.talleb_5edma.network.sendOTPEmail
import sim2.app.talleb_5edma.ui.theme.Talleb_5edmaTheme
import sim2.app.talleb_5edma.util.getSavedOTP

@Composable
fun ForgotPasswordScreen(
    navController: NavController? = null,
    modifier: Modifier,
    snackBarHostState: SnackbarHostState? = null,
    testMode: Boolean = false
) {
    var email by rememberSaveable { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Email format validation
    val isMailValid = remember(email) {
        email.isNotEmpty() &&
                Patterns.EMAIL_ADDRESS.matcher(email).matches() &&
                email.endsWith("@gmail.com") &&
                !email.contains("+")
    }

    // Phone format validation
    val isPhoneValid = remember(email) {
        email.isNotEmpty() &&
                Patterns.PHONE.matcher(email).matches() &&
                email.startsWith("+216") &&
                email.length == 12 &&
                email.substring(4).all { it.isDigit() }
    }

    val isInputValid = remember(email) { isMailValid || isPhoneValid }

    fun handleSendOTP() {
        scope.launch {
            if (isMailValid) {
                isLoading = true
                try {
                    val repository = UserRepository()

                    println(" CatLog : Making API call for email: $email")
                    val emailExistsResponse = repository.checkEmailExists(email)

                    // Now we get the direct EmailExistsResponse
                    println(" CatLog : Email exists response: $emailExistsResponse")
                    println(" CatLog : Exists value: ${emailExistsResponse.exists}")

                    if (emailExistsResponse.exists) {
                        // Email exists, proceed with OTP
                        println(" CatLog : Email exists, sending OTP...")
                        sendOTPEmail(
                            context = context,
                            scope = scope,
                            email = email,
                            onSuccess = {
                                isLoading = false
                                navController?.navigate(Routes.ScreenOTP + "/${getSavedOTP(context)}")
                            },
                            onError = { error ->
                                isLoading = false
                                scope.launch {
                                    snackBarHostState?.showSnackbar(message = error)
                                }
                            }
                        )
                    } else {
                        // Email doesn't exist
                        isLoading = false
                        println(" CatLog : Email does not exist")
                        snackBarHostState?.showSnackbar(
                            message = "This email is not registered in our system. Please check your email or sign up."
                        )
                    }
                } catch (e: Exception) {
                    isLoading = false
                    println(" CatLog : Exception occurred: ${e.message}")
                    snackBarHostState?.showSnackbar(
                        message = "Network error: ${e.message}"
                    )
                }
            } else if (isPhoneValid) {
                snackBarHostState?.showSnackbar(
                    message = "Try Send SMS to this Phone number :)",
                )
            } else {
                snackBarHostState?.showSnackbar(
                    message = "Please enter a valid email address or phone number",
                )
            }
        }
    }

    // Rest of your UI code remains the same...
    Column(modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceEvenly) {
        Row {
            IconButton(
                onClick = { navController?.navigate(route = Routes.Screen1) },
                modifier = Modifier.size(50.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back arrow",
                    tint = Color.Red,
                    modifier = Modifier.size(30.dp)
                )
            }
        }
        Column(
            Modifier
                .fillMaxSize()
                .padding(15.dp),
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            HeaderText("Forgot Password", modifier = Modifier.align(Alignment.Start))
            Text(
                "Please enter your registered email to reset your password",
                fontWeight = FontWeight.Bold
            )
            LoginTextField(
                value = email,
                onValueChange = { email = it },
                modifier = Modifier.fillMaxWidth(0.9f),
                labelText = "Email / Phone",
                leadingIcon = Icons.Default.Email,
                trailingIcon = {
                    when {
                        isLoading -> {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = Color.Red
                            )
                        }
                        email.isNotEmpty() && !isInputValid -> {
                            Icon(
                                painter = painterResource(R.drawable.baseline_error_24),
                                contentDescription = "Error",
                                tint = Color.Red
                            )
                        }
                        isInputValid -> {
                            Icon(
                                painter = painterResource(R.drawable.baseline_check_circle_24),
                                contentDescription = "Valid",
                                tint = Color.Green
                            )
                        }
                    }
                },
                keyboardType = KeyboardType.Email,
                isError = email.isNotEmpty() && !isInputValid
            )

            // Simple validation messages
            if (email.isEmpty()) {
                Text(
                    text = "Must not be empty!",
                    color = Color(0xFFBA1A1A),
                    fontSize = 12.sp,
                    modifier = Modifier.fillMaxWidth(0.9f)
                )
            } else if (!isInputValid) {
                Text(
                    text = if (email.contains("@") && !email.endsWith("@gmail.com")) {
                        "Must use @gmail.com email domain"
                    } else if (email.startsWith("+") && !isPhoneValid) {
                        "Please enter a Tunisian Phone number +216 then 8 digits"
                    } else {
                        "Please enter a valid email address"
                    },
                    color = Color(0xFFBA1A1A),
                    fontSize = 12.sp,
                    modifier = Modifier.fillMaxWidth(0.9f)
                )
            } else if (isInputValid) {
                Text(
                    text = "Format is valid",
                    color = Color.Green,
                    fontSize = 12.sp,
                    modifier = Modifier.fillMaxWidth(0.9f)
                )
            }

            Button(
                onClick = { handleSendOTP() },
                modifier = Modifier.fillMaxWidth(0.9f),
                enabled = isInputValid && !isLoading,
                colors = ButtonDefaults.buttonColors(
                    Color.Red,
                    Color.White
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                }
                Text(if (isLoading) "Checking..." else "Submit", fontSize = 20.sp)
            }

            Text("OR", color = Color.Red, fontSize = 20.sp)

            Button(
                onClick = {
                    scope.launch {
                        if (isMailValid) {
                            snackBarHostState?.showSnackbar(
                                message = "Try Submit button For Emails :)",
                            )
                        } else if (isPhoneValid) {
                            navController?.navigate(Routes.ScreenOTP + "/6789")
                        } else {
                            snackBarHostState?.showSnackbar(
                                message = "Please enter a valid email address or phone number",
                            )
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(0.9f),
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(
                    Color.Red,
                    Color.White
                )
            ) {
                Text("Send SMS", fontSize = 20.sp)
            }

            Spacer(Modifier.fillMaxHeight(0.7f))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ForgotPasswordScreenPreview() {
    val navController = rememberNavController()
    Talleb_5edmaTheme {
        ForgotPasswordScreen(
            navController = navController,
            modifier = Modifier,
            testMode = true
        )
    }
}