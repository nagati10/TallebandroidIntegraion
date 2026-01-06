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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
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
import sim2.app.talleb_5edma.util.*

// Définition de l'énumération pour le sélecteur

// --- Color Palette ROUGE/ROSE VIF ---
val primaryRed = Color(0xFFE91E63)
val brightRed = Color(0xFFFF2D2D)
val softGray = Color(0xFF7C828E)
val fieldBg = Color(0xFFF7F7F9)
val headerHeight = 280.dp
val cardTopPadding = 180.dp
// ----------------------------------------

@Composable
fun LoginScreen(
    navController: NavController? = null,
    modifier: Modifier = Modifier,
    snackBarHostState: SnackbarHostState? = remember { SnackbarHostState() }
) {
    val (checked, setChecked) = rememberSaveable { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val userRepository = remember { UserRepository() }

    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var isFormValid by rememberSaveable { mutableStateOf(false) }
    var selectedUserType by rememberSaveable { mutableStateOf(UserType.Student) }

    val ctx = LocalContext.current

    var isLoading by remember { mutableStateOf(false) }
    var isResponseError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    // Gestion de la connexion Google
    val googleAuthHelper = remember { GoogleAuthHelper(ctx) }
    var isLoadingGoogle by remember { mutableStateOf(false) }

    // Pour forcer l'affichage des erreurs au clic "Log In"
    val (forceValidation, setForceValidation) = remember { mutableStateOf(false) }

    fun handleGoogleSignInResult(result: ActivityResult) {
        if (result.resultCode == Activity.RESULT_OK) {
            isLoadingGoogle = true
            scope.launch {
                try {
                    forceClearAllData(ctx)
                    val idToken = googleAuthHelper.getGoogleToken(result.data)
                    if (idToken != null) {
                        val response = userRepository.loginWithGoogle(idToken)

                        withContext(Dispatchers.Main) {
                            if (response.status == "success" && response.access_token != null) {
                                forceClearAllData(ctx)
                                getSharedPref(ctx).edit {
                                    putString(KEY_EMAIL, response.user?.email ?: "")
                                    putString(KEY_PASSWORD, "")
                                    putString(TOKEN_KEY, response.access_token)
                                    commit()
                                }
                                try {
                                    // Rediriger vers l'accueil entreprise si is_Organization est true
                                    val destination = if (response.user?.isOrganization == true) {
                                        Routes.ScreenHomeEntreprise
                                    } else {
                                        BottomDest.Home.route
                                    }
                                    navController?.navigate(destination) {
                                        popUpTo(0) { inclusive = true }
                                        launchSingleTop = true
                                    }
                                } catch (e: Exception) {
                                    println("LogInScreen - Google login navigation error: ${e.message}")
                                    e.printStackTrace()
                                    // Fallback vers l'accueil normal en cas d'erreur
                                    navController?.navigate(BottomDest.Home.route) {
                                        popUpTo(0) { inclusive = true }
                                        launchSingleTop = true
                                    }
                                }
                            } else {
                                isResponseError = true
                                errorMessage = response.message ?: "Google login failed"
                            }
                        }
                    } else {
                        isResponseError = true
                        errorMessage = "Failed to get Google token"
                    }
                } catch (e: Exception) {
                    isResponseError = true
                    errorMessage = "Network error: ${e.message}"
                } finally {
                    withContext(Dispatchers.Main) {
                        isLoadingGoogle = false
                    }
                }
            }
        }
    }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result -> handleGoogleSignInResult(result) }

    // État de défilement pour la Card
    val scrollState = rememberScrollState()

    Scaffold(
        snackbarHost = { if (snackBarHostState != null) SnackbarHost(snackBarHostState) },
        modifier = Modifier
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFEFEFEF))
                .padding(paddingValues)
        ) {
            // --- Arrière-plan (Image back.jpg) ---
            Image(
                painter = painterResource(id = R.drawable.back),
                contentDescription = "Background pattern",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(headerHeight)
                    .align(Alignment.TopCenter),
                contentScale = ContentScale.FillBounds
            )

            // --- Logo Centralisé dans la zone rouge ---
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "App Logo",
                modifier = Modifier
                    .size(100.dp)
                    .align(Alignment.TopCenter)
                    .offset(y = (headerHeight / 3) - 50.dp)
            )

            // --- Contenu principal (Carte blanche) ---
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 0.dp)
                    .padding(top = cardTopPadding)
                    .align(Alignment.TopCenter),
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 22.dp, vertical = 26.dp)
                        .verticalScroll(scrollState),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Spacer(Modifier.height(15.dp))

                    Text(
                        text = "Welcome Back",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111111)
                    )

                    Spacer(Modifier.height(18.dp))

                    // --- Sélecteur Étudiant/Entreprise ---

                    // Formulaire de Connexion
                    LoginFormContent(
                        email = email,
                        onEmailChange = { email = it },
                        password = password,
                        onPasswordChange = { password = it },
                        onValidationChange = { isFormValid = it },
                        fieldBg = fieldBg,
                        accentColor = brightRed,
                        softGray = softGray,
                        forceValidation = forceValidation
                    )

                    Spacer(Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = checked,
                                onCheckedChange = setChecked,
                                colors = CheckboxDefaults.colors(
                                    checkedColor = primaryRed,
                                    uncheckedColor = softGray,
                                    checkmarkColor = Color.White
                                )
                            )
                            Text("Remember me", fontSize = 12.sp, color = softGray)
                        }
                        Spacer(Modifier.weight(1f))
                        Text(
                            "Forgot password?",
                            modifier = Modifier.clickable {
                                navController?.navigate(Routes.ScreenForgot)
                            },
                            fontSize = 12.sp,
                            color = primaryRed
                        )
                    }

                    if (isResponseError) {
                        Spacer(Modifier.height(6.dp))
                        Text(
                            errorMessage,
                            color = Color(0xFFBA1A1A),
                            fontSize = 12.sp,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(Modifier.height(10.dp))

                    // Bouton Log In
                    Button(
                        onClick = {
                            if (isFormValid) {
                                setForceValidation(false)
                                isLoading = true
                                scope.launch(Dispatchers.IO) {
                                    try {
                                        val response: LoginResponse =
                                            userRepository.login(email, password)

                                        withContext(Dispatchers.Main) {
                                            isLoading = false
                                            if (response.status == "error" || response.access_token == null) {
                                                isResponseError = true
                                                errorMessage = response.message
                                                    ?: "Login failed. Please check your credentials."
                                            } else {
                                                saveUserData(
                                                    ctx,
                                                    email,
                                                    if (checked) password else "",
                                                    response.access_token
                                                )
                                                delay(300)
                                                try {
                                                    // Rediriger vers l'accueil entreprise si is_Organization est true
                                                    val destination = if (response.user?.isOrganization == true) {
                                                        Routes.ScreenHomeEntreprise
                                                    } else {
                                                        BottomDest.Home.route
                                                    }
                                                    navController?.navigate(destination) {
                                                        popUpTo(0) { inclusive = true }
                                                    }
                                                } catch (e: Exception) {
                                                    println("LogInScreen - Navigation error: ${e.message}")
                                                    e.printStackTrace()
                                                    // Fallback vers l'accueil normal en cas d'erreur
                                                    navController?.navigate(BottomDest.Home.route) {
                                                        popUpTo(0) { inclusive = true }
                                                    }
                                                }
                                            }
                                        }
                                    } catch (e: Exception) {
                                        withContext(Dispatchers.Main) {
                                            isLoading = false
                                            isResponseError = true
                                            errorMessage = "Network error: ${e.message}"
                                            snackBarHostState?.showSnackbar(
                                                "Erreur de réseau ou de connexion: ${e.message}"
                                            )
                                        }
                                    }
                                }
                            } else {
                                // Forcer l'affichage des erreurs
                                setForceValidation(true)
                                scope.launch {
                                    snackBarHostState?.showSnackbar("Please fill all required fields correctly.")
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        enabled = !isLoading && !isLoadingGoogle,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Box(
                            Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.horizontalGradient(listOf(brightRed, primaryRed)),
                                    RoundedCornerShape(12.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
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
                                    if (isLoading) "Connexion..." else "Log In",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(30.dp))

                    // Alternative LogIn avec les icônes sociales (définie dans AppComposables.kt)
                    AlternativeLogIn(
                        onIconClick = { index: Int ->
                            when (index) {
                                0 -> scope.launch {
                                    snackBarHostState?.showSnackbar("Facebook Login - Coming soon :)")
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
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    )
                }
            }
        }
    }
}

// ------------------------------------------------------------------
// --- COMPOSANT SÉLECTEUR ÉTUDIANT/ENTREPRISE ---
// ------------------------------------------------------------------

// ------------------------------------------------------------------
// --- COMPOSANT FORMULAIRE (LOGIQUE D'ERREUR AU FOCUS) ---
// ------------------------------------------------------------------

@Composable
fun LoginFormContent(
    email: String,
    onEmailChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    onValidationChange: (Boolean) -> Unit,
    fieldBg: Color,
    accentColor: Color,
    softGray: Color,
    forceValidation: Boolean
) {
    var isPasswordVisible by rememberSaveable { mutableStateOf(false) }

    // États d'interaction utilisateur (false au démarrage)
    var emailTouched by rememberSaveable { mutableStateOf(false) }
    var passwordTouched by rememberSaveable { mutableStateOf(false) }

    val isEmailValid = remember(email) { Patterns.EMAIL_ADDRESS.matcher(email).matches() }
    val isPasswordValid = password.length >= 6
    val isFormValid = email.isNotEmpty() && isEmailValid && password.isNotEmpty() && isPasswordValid

    LaunchedEffect(isFormValid) { onValidationChange(isFormValid) }

    // Déclenche l'affichage des erreurs lors du clic sur le bouton "Log In" si invalide
    LaunchedEffect(forceValidation) {
        if (forceValidation) {
            emailTouched = true
            passwordTouched = true
        }
    }

    Column {
        // Champ Email
        OutlinedTextField(
            value = email,
            onValueChange = { onEmailChange(it) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .onFocusChanged { focusState ->
                    // 👉 dès qu'on clique dans le champ, on active l'affichage des erreurs
                    if (focusState.isFocused) {
                        emailTouched = true
                    }
                },
            placeholder = { Text("Enter email", color = softGray) },
            leadingIcon = { Icon(Icons.Default.Email, null, tint = softGray) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = fieldBg, focusedContainerColor = fieldBg,
                focusedBorderColor = accentColor, unfocusedBorderColor = Color.Transparent,
                errorBorderColor = accentColor, cursorColor = accentColor, errorCursorColor = accentColor,
                focusedLeadingIconColor = accentColor, unfocusedLeadingIconColor = softGray
            )
        )

        // AFFICHE L'ERREUR UNIQUEMENT SI emailTouched EST TRUE
        if (emailTouched) {
            when {
                email.isEmpty() -> Text(
                    "Must not be empty!",
                    color = accentColor,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(start = 12.dp)
                )

                !isEmailValid -> Text(
                    "Please enter a valid email address!",
                    color = accentColor,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(start = 12.dp)
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // Champ Mot de passe
        OutlinedTextField(
            value = password,
            onValueChange = { onPasswordChange(it) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .onFocusChanged { focusState ->
                    if (focusState.isFocused) {
                        passwordTouched = true
                    }
                },
            placeholder = { Text("Password", color = softGray) },
            leadingIcon = { Icon(Icons.Default.Lock, null, tint = softGray) },
            trailingIcon = {
                IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_remove_red_eye_24),
                        contentDescription = null,
                        tint = if (isPasswordVisible) accentColor else softGray
                    )
                }
            },
            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = fieldBg, focusedContainerColor = fieldBg,
                focusedBorderColor = accentColor, unfocusedBorderColor = Color.Transparent,
                errorBorderColor = accentColor, cursorColor = accentColor, errorCursorColor = accentColor,
                focusedLeadingIconColor = accentColor, unfocusedLeadingIconColor = softGray
            )
        )

        if (passwordTouched) {
            when {
                password.isEmpty() -> Text(
                    "Must not be empty!",
                    color = accentColor,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(start = 12.dp)
                )

                !isPasswordValid -> Text(
                    "Password must be at least 6 characters!",
                    color = accentColor,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(start = 12.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenP() {
    LoginScreen(navController = rememberNavController())
}
