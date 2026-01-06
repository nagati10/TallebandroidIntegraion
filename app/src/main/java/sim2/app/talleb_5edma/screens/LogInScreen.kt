package sim2.app.talleb_5edma.screens

import android.app.Activity
import android.util.Patterns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.edit
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import sim2.app.talleb_5edma.BottomDest
import sim2.app.talleb_5edma.R
import sim2.app.talleb_5edma.Routes
import sim2.app.talleb_5edma.network.GoogleAuthHelper
import sim2.app.talleb_5edma.network.LoginResponse
import sim2.app.talleb_5edma.network.UserRepository
import sim2.app.talleb_5edma.ui.theme.Talleb_5edmaTheme
import sim2.app.talleb_5edma.util.KEY_EMAIL
import sim2.app.talleb_5edma.util.KEY_PASSWORD
import sim2.app.talleb_5edma.util.TOKEN_KEY
import sim2.app.talleb_5edma.util.forceClearAllData
import sim2.app.talleb_5edma.util.getSharedPref
import sim2.app.talleb_5edma.util.getToken
import sim2.app.talleb_5edma.util.getUserInfoForDebug
import sim2.app.talleb_5edma.util.saveUserData

@Composable
fun LoginScreen(
    navController: NavController? = null,
    modifier: Modifier = Modifier,
    snackBarHostState: SnackbarHostState? = null
) {
    val (checked, setChecked) = rememberSaveable { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var isFormValid by rememberSaveable { mutableStateOf(false) }

    val ctx = LocalContext.current

    var isLoading by remember { mutableStateOf(false) }
    var isResponseError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    // toggle Student / Company (design only for the moment)
    var isStudentSelected by rememberSaveable { mutableStateOf(true) }

    // colors
    val red = Color(0xFFFF2D2D)
    val softGray = Color(0xFF7C828E)
    val lightBg = Color(0xFFFFF4F5)

    // Google Sign-In setup
    val googleAuthHelper = remember { GoogleAuthHelper(ctx) }
    var isLoadingGoogle by remember { mutableStateOf(false) }

    fun handleGoogleSignInResult(result: ActivityResult) {
        if (result.resultCode == Activity.RESULT_OK) {
            isLoadingGoogle = true
            scope.launch {
                try {
                    forceClearAllData(ctx)
                    val idToken = googleAuthHelper.getGoogleToken(result.data)
                    println("CatLog : idToken: $idToken")
                    if (idToken != null) {
                        val repository = UserRepository()
                        val response = repository.loginWithGoogle(idToken)

                        if (response.status == "success" && response.access_token != null) {
                            forceClearAllData(ctx)

                            getSharedPref(ctx).edit {
                                putString(KEY_EMAIL, response.user!!.email ?: "")
                                putString(KEY_PASSWORD, "")
                                putString(TOKEN_KEY, response.access_token)
                                commit()
                            }

                            println("CatLog : Google login successful")
                            println("CatLog : Immediately after save - ${getToken(ctx)}")

                            navController?.navigate(BottomDest.Home.route) {
                                popUpTo(0) {
                                    inclusive = true
                                    saveState = false
                                }
                                launchSingleTop = true
                                restoreState = false
                            }
                        } else {
                            isResponseError = true
                            errorMessage = response.message ?: "Google login failed"
                        }
                    } else {
                        isResponseError = true
                        errorMessage = "Failed to get Google token"
                    }
                } catch (e: Exception) {
                    isResponseError = true
                    errorMessage = "Network error: ${e.message}"
                } finally {
                    isLoadingGoogle = false
                }
            }
        }
    }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        handleGoogleSignInResult(result)
    }

    val gradient = Brush.verticalGradient(
        colors = listOf(lightBg, Color.White),
        startY = 0f,
        endY = Float.POSITIVE_INFINITY
    )

    Box(
        modifier
            .fillMaxSize()
            .background(gradient)
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(Modifier.height(40.dp))

            // ====== HEADER (logo + name + tagline) ======
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "Taleb 5edma",
                    modifier = Modifier.size(70.dp)
                )
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Taleb 5edma",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF222222)
                    )
                    Text(
                        text = "Pour les étudiants",
                        fontSize = 13.sp,
                        color = softGray
                    )
                }
            }

            Spacer(Modifier.height(18.dp))

            Text(
                text = "Trouvez votre opportunité parfaite",
                fontSize = 13.sp,
                color = softGray
            )

            Spacer(Modifier.height(24.dp))

            // ====== MAIN CARD ======
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 22.dp, horizontal = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Connexion",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color(0xFF222222)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Connectez-vous à votre compte",
                        fontSize = 13.sp,
                        color = softGray
                    )

                    Spacer(Modifier.height(16.dp))

                    // --- segmented Étudiant / Entreprise ---
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = Color(0xFFF3F4F7),
                                shape = RoundedCornerShape(50.dp)
                            )
                            .padding(3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp)
                                .background(
                                    color = if (isStudentSelected) red else Color.Transparent,
                                    shape = RoundedCornerShape(50.dp)
                                )
                                .clickable { isStudentSelected = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Étudiant",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (isStudentSelected) Color.White else softGray
                            )
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp)
                                .background(
                                    color = if (!isStudentSelected) red else Color.Transparent,
                                    shape = RoundedCornerShape(50.dp)
                                )
                                .clickable { isStudentSelected = false },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Entreprise",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (!isStudentSelected) Color.White else softGray
                            )
                        }
                    }

                    Spacer(Modifier.height(18.dp))

                    // --- email + password ---
                    LoginForm(
                        onEmailChange = { email = it },
                        onPasswordChange = { password = it },
                        onValidationChange = { isValid -> isFormValid = isValid }
                    )

                    Spacer(Modifier.height(8.dp))

                    // --- remember + forgot ---
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = checked,
                                onCheckedChange = setChecked,
                                colors = CheckboxDefaults.colors(
                                    checkedColor = colorResource(R.color.CheckColor),
                                    uncheckedColor = colorResource(R.color.CheckColor),
                                    checkmarkColor = colorResource(R.color.onCheckColor)
                                )
                            )
                            Text(
                                text = "Remember me",
                                fontSize = 13.sp,
                                color = softGray
                            )
                        }
                        Spacer(Modifier.weight(1f))
                        Text(
                            text = "Forgot password?",
                            modifier = Modifier.clickable {
                                navController?.navigate(Routes.ScreenForgot)
                            },
                            fontSize = 13.sp,
                            color = red
                        )
                    }

                    if (isResponseError) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = errorMessage,
                            color = Color(0xFFBA1A1A),
                            textAlign = TextAlign.Center,
                            fontSize = 12.sp,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    // --- login button ---
                    Button(
                        onClick = {
                            forceClearAllData(ctx)
                            println("CatLog: LoginScreen - Cleared all data before login")

                            if (isFormValid) {
                                isLoading = true
                                scope.launch {
                                    try {
                                        val repository = UserRepository()
                                        val response: LoginResponse =
                                            repository.login(email, password)
                                        val emailExistsResponse =
                                            repository.checkEmailExists(email)
                                        println(" CatLog : login response: $response")
                                        println(" CatLog : login Exists value: ${emailExistsResponse.exists}")

                                        if (emailExistsResponse.exists) {
                                            withContext(Dispatchers.Main) {
                                                isLoading = false
                                                if (response.status == "error" || response.access_token == null) {
                                                    isResponseError = true
                                                    errorMessage =
                                                        response.message ?: "Login failed"
                                                } else {
                                                    saveUserData(
                                                        ctx,
                                                        email,
                                                        if (checked) password else "",
                                                        response.access_token
                                                    )
                                                    println("CatLog: Token saved after login: ${response.access_token.take(10)}...")
                                                    println("CatLog: After regular login - ${getUserInfoForDebug(ctx)}")

                                                    delay(500)

                                                    navController?.navigate(BottomDest.Home.route) {
                                                        popUpTo(0) { inclusive = true }
                                                    }
                                                }
                                            }
                                        } else {
                                            isLoading = false
                                            isResponseError = true
                                            errorMessage = "User does not exist"
                                        }
                                    } catch (e: Exception) {
                                        withContext(Dispatchers.Main) {
                                            isLoading = false
                                            isResponseError = true
                                            errorMessage = "Network error: ${e.message}"
                                        }
                                    }
                                }
                            } else {
                                scope.launch {
                                    snackBarHostState?.showSnackbar(
                                        message = "You have some errors in your input!",
                                    )
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        enabled = !isLoading && !isLoadingGoogle,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = red,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier
                                    .size(20.dp)
                                    .padding(end = 8.dp),
                                strokeWidth = 2.dp
                            )
                        }
                        Text(
                            text = if (isLoading) "Connexion..." else "Se connecter",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(Modifier.height(90.dp)) // espace pour laisser place au bloc Google/Facebook en bas
        }

        // ====== GOOGLE / FACEBOOK + SIGNUP (même logique qu'avant) ======
        AlternativeLogIn(
            onIconClick = { index ->
                when (index) {
                    0 -> {
                        scope.launch {
                            snackBarHostState?.showSnackbar(
                                message = "Coming soon :)",
                            )
                        }
                    }

                    1 -> {
                        val signInIntent =
                            googleAuthHelper.getGoogleSignInClient().signInIntent
                        googleSignInLauncher.launch(signInIntent)
                    }
                }
            },
            onSingUpClick = { navController?.navigate(Routes.Screen2) },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp)
        )
    }
}

@Composable
fun LoginForm(
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onValidationChange: (Boolean) -> Unit
) {
    var mail by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }

    val isEmailValid = remember(mail) {
        Patterns.EMAIL_ADDRESS.matcher(mail).matches()
    }

    val isPasswordValid = password.length >= 6
    val isFormValid = isEmailValid && isPasswordValid
    var isPasswordVisible by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(isFormValid) { onValidationChange(isFormValid) }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        OutlinedTextField(
            value = mail,
            onValueChange = {
                mail = it
                onEmailChange(it)
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Email") },
            leadingIcon = {
                Icon(Icons.Default.Email, contentDescription = "Email icon")
            },
            trailingIcon = {
                if (mail.isNotEmpty() && !isEmailValid) {
                    Icon(
                        painter = painterResource(R.drawable.baseline_error_24),
                        contentDescription = "Error",
                        tint = Color(0xFFBA1A1A)
                    )
                }
            },
            isError = mail.isNotEmpty() && !isEmailValid,
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )

        if (mail.isEmpty()) {
            Text(
                text = "Must not be empty",
                color = Color.Red,
                fontSize = 12.sp,
                modifier = Modifier.fillMaxWidth()
            )
        } else if (!isEmailValid) {
            Text(
                text = "Please enter a valid email address",
                color = Color(0xFFBA1A1A),
                fontSize = 12.sp,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                onPasswordChange(it)
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Password") },
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
                            modifier = Modifier.size(24.dp),
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
                text = "Must not be empty",
                color = Color(0xFFBA1A1A),
                fontSize = 12.sp,
                modifier = Modifier.fillMaxWidth()
            )
        } else if (!isPasswordValid) {
            Text(
                text = "Password must be at least 6 characters",
                color = Color(0xFFBA1A1A),
                fontSize = 12.sp,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    val navController = rememberNavController()
    Talleb_5edmaTheme {
        LoginScreen(navController = navController, modifier = Modifier.fillMaxSize())
    }
}
