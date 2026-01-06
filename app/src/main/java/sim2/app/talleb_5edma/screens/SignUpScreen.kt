package sim2.app.talleb_5edma.screens

import android.graphics.Bitmap
import android.net.Uri
import android.util.Patterns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
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
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import sim2.app.talleb_5edma.R
import sim2.app.talleb_5edma.Routes
import sim2.app.talleb_5edma.network.CreateUserRequest
import sim2.app.talleb_5edma.network.UserRepository
import sim2.app.talleb_5edma.util.FileUtils
import androidx.compose.ui.focus.onFocusChanged

// -------------------------------------------------------------
//  ENUM + SELECTOR Étudiant / Entreprise
// -------------------------------------------------------------

enum class UserType {
    Student, Company
}

@Composable
fun UserTypeSelector(
    selectedType: UserType,
    onTypeSelected: (UserType) -> Unit,
    accentColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .background(Color(0xFFE0E0E0), RoundedCornerShape(12.dp)),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(
            onClick = { onTypeSelected(UserType.Student) },
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (selectedType == UserType.Student) accentColor else Color.Transparent,
                contentColor = if (selectedType == UserType.Student) Color.White else Color.Gray
            ),
            shape = RoundedCornerShape(12.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
        ) {
            Text("Étudiant", fontWeight = FontWeight.SemiBold)
        }

        Button(
            onClick = { onTypeSelected(UserType.Company) },
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (selectedType == UserType.Company) accentColor else Color.Transparent,
                contentColor = if (selectedType == UserType.Company) Color.White else Color.Gray
            ),
            shape = RoundedCornerShape(12.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
        ) {
            Text("Entreprise", fontWeight = FontWeight.SemiBold)
        }
    }
}

// -------------------------------------------------------------
//  ÉCRAN SIGN UP
// -------------------------------------------------------------

@Composable
fun SignUpScreen(
    navController: NavController? = null,
    modifier: Modifier = Modifier.fillMaxSize()
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val userRepository = remember { UserRepository() }
    val snackBarHostState = remember { SnackbarHostState() }

    var isFormValid by rememberSaveable { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    var selectedUserType by remember { mutableStateOf(UserType.Student) }

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }

    val primaryRed = Color(0xFFE91E63)
    val brightRed = Color(0xFFFF2D2D)
    val softGray = Color(0xFF7C828E)
    val fieldBg = Color(0xFFF7F7F9)
    val errorRed = Color(0xFFE53935)

    val headerHeight = 280.dp
    val cardTopPadding = 180.dp

    val scrollState = rememberScrollState()

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            try {
                bitmap = FileUtils.uriToBitmap(context, it)
                selectedImageUri = it
                scope.launch { snackBarHostState.showSnackbar("Profile image selected") }
            } catch (e: Exception) {
                scope.launch { snackBarHostState.showSnackbar("Image load failed") }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackBarHostState) }
    ) { paddingValues ->
        Box(
            modifier = modifier
                .background(Color(0xFFEFEFEF))
                .padding(paddingValues)
        ) {
            Image(
                painter = painterResource(id = R.drawable.back),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(headerHeight)
                    .align(Alignment.TopCenter),
                contentScale = ContentScale.FillBounds
            )

            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = null,
                modifier = Modifier
                    .size(100.dp)
                    .align(Alignment.TopCenter)
                    .offset(y = headerHeight / 3 - 50.dp)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = cardTopPadding)
                    .align(Alignment.TopCenter),
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 22.dp, vertical = 24.dp)
                        .verticalScroll(scrollState),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {


                    Spacer(Modifier.height(30.dp))

                    Text(
                        text = "Create Your Account",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111111)
                    )

                    Spacer(Modifier.height(12.dp))

                    // Sélecteur Étudiant / Entreprise
                    UserTypeSelector(
                        selectedType = selectedUserType,
                        onTypeSelected = { selectedUserType = it },
                        accentColor = primaryRed
                    )

                    Spacer(Modifier.height(24.dp))

                    // Image de profil
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { galleryLauncher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        if (bitmap != null) {
                            Image(
                                bitmap = bitmap!!.asImageBitmap(),
                                contentDescription = "Profile image",
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Add profile image",
                                modifier = Modifier.size(70.dp),
                                tint = softGray
                            )
                        }

                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(34.dp)
                                .background(brightRed, CircleShape)
                                .padding(6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.baseline_photo_camera_24),
                                contentDescription = "Add photo",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // Formulaire
                    SignUpFormContent(
                        fullName = fullName,
                        onFullNameChange = { fullName = it },
                        email = email,
                        onEmailChange = { email = it },
                        password = password,
                        onPasswordChange = { password = it },
                        confirmPassword = confirmPassword,
                        onConfirmPasswordChange = { confirmPassword = it },
                        phone = phone,
                        onPhoneChange = { phone = it },
                        onValidationChange = { isFormValid = it },
                        fieldBg = fieldBg,
                        accentColor = brightRed,
                        errorColor = errorRed,
                        greyText = softGray
                    )

                    Spacer(Modifier.height(18.dp))

                    // Bouton Get Started
                    Button(
                        onClick = {
                            if (!isFormValid) {
                                scope.launch {
                                    snackBarHostState.showSnackbar("Please fill all required fields correctly.")
                                }
                                return@Button
                            }

                            isLoading = true
                            scope.launch(Dispatchers.IO) {
                                try {
                                    val userReq = CreateUserRequest(
                                        nom = fullName,
                                        email = email,
                                        password = password,
                                        confirmPassword = confirmPassword,
                                        contact = phone,
                                        isOrganization = selectedUserType == UserType.Company
                                    )

                                    val res = userRepository.createUser(
                                        userRequest = userReq,
                                        imageBytes = selectedImageUri?.let {
                                            FileUtils.uriToByteArray(context, it)
                                        },
                                        imageFileName = selectedImageUri?.let {
                                            FileUtils.getFileName(context, it)
                                        }
                                    )

                                    withContext(Dispatchers.Main) {
                                        isLoading = false
                                        if (res._id != null) {
                                            snackBarHostState.showSnackbar("Account created successfully!")
                                            navController?.navigate(Routes.Screen1)
                                        } else {
                                            snackBarHostState.showSnackbar("Registration failed.")
                                        }
                                    }
                                } catch (e: Exception) {
                                    withContext(Dispatchers.Main) {
                                        isLoading = false
                                        snackBarHostState.showSnackbar("Error: ${e.message}")
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        enabled = !isLoading,
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
                            if (isLoading) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    "Get Started",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text("Already have an account? ", fontSize = 13.sp, color = softGray)
                        Text(
                            "Log In",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = brightRed,
                            modifier = Modifier.clickable {
                                navController?.navigate(Routes.Screen1)
                            }
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
//  FORMULAIRE D'INSCRIPTION
// -------------------------------------------------------------

@Composable
fun SignUpFormContent(
    fullName: String, onFullNameChange: (String) -> Unit,
    email: String, onEmailChange: (String) -> Unit,
    password: String, onPasswordChange: (String) -> Unit,
    confirmPassword: String, onConfirmPasswordChange: (String) -> Unit,
    phone: String, onPhoneChange: (String) -> Unit,
    onValidationChange: (Boolean) -> Unit,
    fieldBg: Color, accentColor: Color, errorColor: Color, greyText: Color
) {
    var isPasswordVisible by rememberSaveable { mutableStateOf(false) }
    var isConfirmPasswordVisible by rememberSaveable { mutableStateOf(false) }

    // 👉 Flags pour savoir si l'utilisateur a cliqué dans le champ
    var fullNameTouched by rememberSaveable { mutableStateOf(false) }
    var emailTouched by rememberSaveable { mutableStateOf(false) }
    var passwordTouched by rememberSaveable { mutableStateOf(false) }
    var confirmTouched by rememberSaveable { mutableStateOf(false) }
    var phoneTouched by rememberSaveable { mutableStateOf(false) }

    val isFullNameValid = fullName.isNotEmpty()
    val isEmailValid = Patterns.EMAIL_ADDRESS.matcher(email).matches()
    val isPasswordValid = password.length >= 6
    val isConfirmPasswordValid =
        confirmPassword == password && confirmPassword.isNotEmpty() && isPasswordValid
    val isPhoneValid = phone.length >= 8

    val isFormValid =
        isFullNameValid && isEmailValid && isPasswordValid && isConfirmPasswordValid && isPhoneValid

    LaunchedEffect(isFormValid) { onValidationChange(isFormValid) }

    Column(modifier = Modifier.fillMaxWidth()) {

        // ---------- FULL NAME ----------
        OutlinedTextField(
            value = fullName,
            onValueChange = {
                onFullNameChange(it)
                if (!fullNameTouched) fullNameTouched = true      // tape → touché
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .onFocusChanged { state ->
                    if (state.isFocused) fullNameTouched = true   // clic → touché
                },
            placeholder = { Text("Enter full name", color = greyText) },
            leadingIcon = { Icon(Icons.Default.Person, null, tint = greyText) },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = fieldBg, focusedContainerColor = fieldBg,
                focusedBorderColor = accentColor, unfocusedBorderColor = Color.Transparent,
                errorBorderColor = errorColor, cursorColor = accentColor,
                focusedLeadingIconColor = accentColor, unfocusedLeadingIconColor = greyText
            )
        )
        if (fullNameTouched) {                      // ❗ affiché seulement après clic
            if (fullName.isEmpty()) {
                Text(
                    "Must not be empty!",
                    color = errorColor,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(start = 12.dp)
                )
            } else {
                Spacer(Modifier.height(11.dp))
            }
        }

        Spacer(Modifier.height(1.dp))

        // ---------- EMAIL ----------
        OutlinedTextField(
            value = email,
            onValueChange = {
                onEmailChange(it)
                if (!emailTouched) emailTouched = true
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .onFocusChanged { state ->
                    if (state.isFocused) emailTouched = true
                },
            placeholder = { Text("Enter email", color = greyText) },
            leadingIcon = { Icon(Icons.Default.Email, null, tint = greyText) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = fieldBg, focusedContainerColor = fieldBg,
                focusedBorderColor = accentColor, unfocusedBorderColor = Color.Transparent,
                errorBorderColor = errorColor, cursorColor = accentColor,
                focusedLeadingIconColor = accentColor, unfocusedLeadingIconColor = greyText
            )
        )
        if (emailTouched) {
            when {
                email.isEmpty() -> Text(
                    "Must not be empty!",
                    color = errorColor,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(start = 12.dp)
                )

                !isEmailValid -> Text(
                    "Please enter a valid email address!",
                    color = errorColor,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(start = 12.dp)
                )

                else -> Spacer(Modifier.height(11.dp))
            }
        }

        Spacer(Modifier.height(1.dp))

        // ---------- PASSWORD ----------
        OutlinedTextField(
            value = password,
            onValueChange = {
                onPasswordChange(it)
                if (!passwordTouched) passwordTouched = true
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .onFocusChanged { state ->
                    if (state.isFocused) passwordTouched = true
                },
            placeholder = { Text("Enter password", color = greyText) },
            leadingIcon = { Icon(Icons.Default.Lock, null, tint = greyText) },
            trailingIcon = {
                IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_remove_red_eye_24),
                        contentDescription = null,
                        tint = if (isPasswordVisible) accentColor else greyText
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
                errorBorderColor = errorColor, cursorColor = accentColor,
                focusedLeadingIconColor = accentColor, unfocusedLeadingIconColor = greyText
            )
        )
        if (passwordTouched) {
            when {
                password.isEmpty() -> Text(
                    "Must not be empty!",
                    color = errorColor,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(start = 12.dp)
                )

                !isPasswordValid -> Text(
                    "Password must be at least 6 characters!",
                    color = errorColor,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(start = 12.dp)
                )

                else -> Spacer(Modifier.height(11.dp))
            }
        }

        Spacer(Modifier.height(1.dp))

        // ---------- CONFIRM PASSWORD ----------
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = {
                onConfirmPasswordChange(it)
                if (!confirmTouched) confirmTouched = true
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .onFocusChanged { state ->
                    if (state.isFocused) confirmTouched = true
                },
            placeholder = { Text("Confirm password", color = greyText) },
            leadingIcon = { Icon(Icons.Default.Lock, null, tint = greyText) },
            trailingIcon = {
                IconButton(onClick = { isConfirmPasswordVisible = !isConfirmPasswordVisible }) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_remove_red_eye_24),
                        contentDescription = null,
                        tint = if (isConfirmPasswordVisible) accentColor else greyText
                    )
                }
            },
            visualTransformation = if (isConfirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = fieldBg, focusedContainerColor = fieldBg,
                focusedBorderColor = accentColor, unfocusedBorderColor = Color.Transparent,
                errorBorderColor = errorColor, cursorColor = accentColor,
                focusedLeadingIconColor = accentColor, unfocusedLeadingIconColor = greyText
            )
        )
        if (confirmTouched) {
            when {
                confirmPassword.isEmpty() -> Text(
                    "Must not be empty!",
                    color = errorColor,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(start = 12.dp)
                )

                !isConfirmPasswordValid -> Text(
                    "Passwords do not match!",
                    color = errorColor,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(start = 12.dp)
                )

                else -> Spacer(Modifier.height(11.dp))
            }
        }

        Spacer(Modifier.height(1.dp))

        // ---------- PHONE ----------
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = "+216",
                onValueChange = { },
                enabled = false,
                modifier = Modifier
                    .width(90.dp)
                    .height(56.dp),
                leadingIcon = { Icon(Icons.Default.Call, null, tint = greyText) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = fieldBg, focusedContainerColor = fieldBg,
                    disabledBorderColor = Color.Transparent,
                    disabledContainerColor = fieldBg,
                    disabledLeadingIconColor = greyText,
                    disabledTextColor = Color.Black
                )
            )
            Spacer(Modifier.width(8.dp))

            OutlinedTextField(
                value = phone,
                onValueChange = {
                    onPhoneChange(it)
                    if (!phoneTouched) phoneTouched = true
                },
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
                    .onFocusChanged { state ->
                        if (state.isFocused) phoneTouched = true
                    },
                placeholder = { Text("Phone number", color = greyText) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = fieldBg, focusedContainerColor = fieldBg,
                    focusedBorderColor = accentColor, unfocusedBorderColor = Color.Transparent,
                    errorBorderColor = errorColor, cursorColor = accentColor
                )
            )
        }
        if (phoneTouched) {
            when {
                phone.isEmpty() -> Text(
                    "Phone number is required!",
                    color = errorColor,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(start = 12.dp)
                )

                !isPhoneValid -> Text(
                    "Please enter a valid phone number (min 8 digits)!",
                    color = errorColor,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(start = 12.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SignUpScreenPreview() {
    SignUpScreen(navController = rememberNavController())
}
