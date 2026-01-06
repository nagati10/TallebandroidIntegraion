package sim2.app.talleb_5edma.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import sim2.app.talleb_5edma.R
import sim2.app.talleb_5edma.Routes
import sim2.app.talleb_5edma.network.UserRepository
import sim2.app.talleb_5edma.ui.theme.Talleb_5edmaTheme
import sim2.app.talleb_5edma.util.getSavedEmail

@Composable
fun ResetPasswordScreen(
    navController: NavController? = null,
    modifier: Modifier,
    snackBarHostState: SnackbarHostState? = null,
    email: String? = null // Optional: email can still be passed directly
) {
    var isFormValid by rememberSaveable { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var successMessage by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()
    val userRepository = remember { UserRepository() }
    val context = LocalContext.current

    // Move password state management to the parent component
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }

    // Get email from local storage if not provided directly
    val userEmail = email ?: getSavedEmail(context)

    Column(modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceEvenly) {
        Row {
            IconButton(
                onClick = { navController?.popBackStack() },
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
            HeaderText("Reset Password", modifier = Modifier.align(Alignment.Start))

            if (userEmail.isNotEmpty()) {
                Text(
                    "Reset password for: $userEmail",
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray,
                    modifier = Modifier.fillMaxWidth(0.9f)
                )
            } else {
                Text(
                    "No email found. Please go back and enter your email again.",
                    fontWeight = FontWeight.Bold,
                    color = Color.Red,
                    modifier = Modifier.fillMaxWidth(0.9f)
                )
            }

            Text(
                "Please enter your new password and confirm it",
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(0.9f)
            )

            // Pass the current state and state setters to ResetForm
            ResetForm(
                password = password,
                confirmPassword = confirmPassword,
                onPasswordChange = { newPassword ->
                    password = newPassword
                },
                onConfirmPasswordChange = { newConfirmPassword ->
                    confirmPassword = newConfirmPassword
                },
                onValidationChange = { isValid ->
                    isFormValid = isValid
                }
            )

            // Show error message if any
            if (errorMessage.isNotEmpty()) {
                Text(
                    text = errorMessage,
                    color = Color.Red,
                    modifier = Modifier.fillMaxWidth(0.9f)
                )
            }

            // Show success message if any
            if (successMessage.isNotEmpty()) {
                Text(
                    text = successMessage,
                    color = Color.Green,
                    modifier = Modifier.fillMaxWidth(0.9f)
                )
            }

            Button(
                onClick = {
                    if (isFormValid && userEmail.isNotEmpty()) {
                        isLoading = true
                        errorMessage = ""
                        successMessage = ""

                        scope.launch {
                            try {
                                val response = userRepository.resetPassword(userEmail, password)

                                if (response.message!!.contains("success", ignoreCase = true)) {
                                    successMessage = "Password reset successfully!"
                                    // Navigate to login screen after a short delay
                                    kotlinx.coroutines.delay(2000) // Show success message for 2 seconds
                                    navController?.navigate(Routes.Screen1) {
                                        popUpTo(Routes.Screen1) {
                                            inclusive = true
                                        }
                                    }
                                } else {
                                    errorMessage = response.message!!
                                }
                            } catch (e: Exception) {
                                errorMessage = "Network error: ${e.message}"
                            } finally {
                                isLoading = false
                            }
                        }
                    } else if (userEmail.isEmpty()) {
                        scope.launch {
                            snackBarHostState?.showSnackbar(
                                message = "Email is required for password reset!"
                            )
                        }
                    } else {
                        scope.launch {
                            snackBarHostState?.showSnackbar(
                                message = "You have some errors in your input!"
                            )
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(0.9f),
                enabled = !isLoading && isFormValid && userEmail.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(
                    Color.Red,
                    Color.White
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier
                            .size(20.dp)
                            .padding(end = 8.dp)
                    )
                }
                Text(
                    if (isLoading) "Resetting..." else "Reset Password",
                    fontSize = 20.sp
                )
            }

            Spacer(Modifier.fillMaxHeight(0.7f))
        }
    }
}

@Composable
fun ResetForm(
    password: String,
    confirmPassword: String,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onValidationChange: (Boolean) -> Unit
) {
    var isPasswordVisible by rememberSaveable { mutableStateOf(false) }
    var isConfirmPasswordVisible by rememberSaveable { mutableStateOf(false) }

    val isPasswordValid = password.length >= 6
    val isConfirmPasswordValid = password == confirmPassword && confirmPassword.isNotEmpty()
    val isFormValid = isPasswordValid && isConfirmPasswordValid

    LaunchedEffect(isFormValid) {
        onValidationChange(isFormValid)
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = password,
            onValueChange = onPasswordChange,
            modifier = Modifier.fillMaxWidth(0.9f),
            label = { Text("New Password") },
            leadingIcon = {
                Icon(Icons.Default.Lock, contentDescription = "Password icon")
            },
            trailingIcon = {
                if (password.isNotEmpty() && !isPasswordValid) {
                    Icon(
                        painter = painterResource(R.drawable.baseline_error_24),
                        contentDescription = "Error",
                        tint = Color(0xFFBA1A1A)
                    )
                } else {
                    IconButton(
                        onClick = { isPasswordVisible = !isPasswordVisible }
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_remove_red_eye_24),
                            tint = if (isPasswordVisible) Color.Gray else Color.Red,
                            modifier = Modifier.size(30.dp),
                            contentDescription = if (isPasswordVisible) "Hide password" else "Show password"
                        )
                    }
                }
            },
            visualTransformation = if (isPasswordVisible) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            isError = password.isNotEmpty() && !isPasswordValid,
            singleLine = true
        )

        if (password.isEmpty()) {
            Text(
                text = "Must not be empty!",
                color = Color(0xFFBA1A1A),
                fontSize = 12.sp,
                modifier = Modifier.fillMaxWidth(0.9f)
            )
        } else if (!isPasswordValid) {
            Text(
                text = "Password must be at least 6 characters",
                color = Color(0xFFBA1A1A),
                fontSize = 12.sp,
                modifier = Modifier.fillMaxWidth(0.9f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = onConfirmPasswordChange,
            modifier = Modifier.fillMaxWidth(0.9f),
            label = { Text("Confirm Password") },
            leadingIcon = {
                Icon(Icons.Default.Lock, contentDescription = "Confirm password icon")
            },
            trailingIcon = {
                if (confirmPassword.isNotEmpty() && !isConfirmPasswordValid) {
                    Icon(
                        painter = painterResource(R.drawable.baseline_error_24),
                        contentDescription = "Error",
                        tint = Color(0xFFBA1A1A)
                    )
                } else {
                    IconButton(
                        onClick = { isConfirmPasswordVisible = !isConfirmPasswordVisible }
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_remove_red_eye_24),
                            tint = if (isConfirmPasswordVisible) Color.Gray else Color.Red,
                            modifier = Modifier.size(30.dp),
                            contentDescription = if (isConfirmPasswordVisible) "Hide password" else "Show password"
                        )
                    }
                }
            },
            visualTransformation = if (isConfirmPasswordVisible) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            isError = confirmPassword.isNotEmpty() && !isConfirmPasswordValid,
            singleLine = true
        )

        if (confirmPassword.isEmpty()) {
            Text(
                text = "Must not be empty!",
                color = Color(0xFFBA1A1A),
                fontSize = 12.sp,
                modifier = Modifier.fillMaxWidth(0.9f)
            )
        } else if (!isConfirmPasswordValid) {
            Text(
                text = "Must be the same password!",
                color = Color(0xFFBA1A1A),
                fontSize = 12.sp,
                modifier = Modifier.fillMaxWidth(0.9f)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ResetPasswordScreenPreview() {
    Talleb_5edmaTheme {
        ResetPasswordScreen(
            modifier = Modifier.fillMaxSize(),
            email = "user@example.com"
        )
    }
}