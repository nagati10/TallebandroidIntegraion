package sim2.app.talleb_5edma.screens

import android.graphics.Bitmap
import android.net.Uri
import android.util.Patterns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import kotlinx.coroutines.*
import sim2.app.talleb_5edma.R
import sim2.app.talleb_5edma.Routes
import sim2.app.talleb_5edma.models.User
import sim2.app.talleb_5edma.network.UpdateUserRequest
import sim2.app.talleb_5edma.network.UserRepository
import sim2.app.talleb_5edma.network.sendOTPEmail
import sim2.app.talleb_5edma.util.BASE_URL
import sim2.app.talleb_5edma.util.FileUtils
import sim2.app.talleb_5edma.util.forceClearAllData
import sim2.app.talleb_5edma.util.getSavedOTP
import sim2.app.talleb_5edma.util.getToken

/* -------------------------------------------------------------------------- */
/* UI PALETTE                                   */
/* -------------------------------------------------------------------------- */
private val Primary = Color(0xFF000000)
private val Accent = Color(0xFF000000)

private val CardBg = Color(0xD2FFFFFF)

/* -------------------------------------------------------------------------- */
/* MAIN SCREEN                                   */
/* -------------------------------------------------------------------------- */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    navController: NavController? = null,
    user: User? = null,
    token: String? = null,
    snackBarHostState: SnackbarHostState? = null,
    onProfileUpdated: (User) -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val actualToken by remember {
        derivedStateOf {
            token?.takeIf { it.isNotEmpty() }
                ?: getToken(context).takeIf { it.isNotEmpty() } ?: ""
        }
    }

    var currentUser by remember { mutableStateOf(user) }
    var isLoadingUser by remember { mutableStateOf(user == null) }
    var fetchError by remember { mutableStateOf<String?>(null) }

    var fullName by remember { mutableStateOf(user?.nom ?: "") }
    var email by remember { mutableStateOf(user?.email ?: "") }
    var contact by remember { mutableStateOf(user?.contact ?: "") }

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    var currentImageUrl by remember { mutableStateOf(user?.image ?: "") }

    var isLoading by remember { mutableStateOf(false) }
    var isUpdatingImage by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var successMessage by remember { mutableStateOf("") }
    var isChangingPassword by remember { mutableStateOf(false) }

    LaunchedEffect(actualToken) {
        if (actualToken.isEmpty()) {
            currentUser = null
            fullName = ""; email = ""; contact = ""; currentImageUrl = ""
            selectedImageUri = null; bitmap = null; fetchError = null; isLoadingUser = false
        }
    }

    LaunchedEffect(actualToken) {
        if (actualToken.isNotEmpty() && currentUser == null) {
            isLoadingUser = true
            try {
                val repo = UserRepository()
                val resp = repo.getCurrentUser(actualToken)
                if (resp._id != null) {
                    val u = User(
                        _id = resp._id,
                        nom = resp.nom,
                        email = resp.email,
                        contact = resp.contact,
                        role = resp.role,
                        image = resp.image,
                        password = resp.password,
                        createdAt = resp.createdAt,
                        updatedAt = resp.updatedAt,
                    )
                    currentUser = u
                    fullName = u.nom ?: ""
                    email = u.email ?: ""
                    contact = u.contact ?: ""
                    currentImageUrl = u.image ?: ""
                } else {
                    fetchError = resp.message ?: "Failed to load profile"
                    if (resp.status == 401 || resp.status == 403) {
                        forceClearAllData(context)
                        navController?.navigate(Routes.Screen1) { popUpTo(0) }
                    }
                }
            } catch (e: Exception) {
                fetchError = "Network error: ${e.message}"
            } finally { isLoadingUser = false }
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            try {
                bitmap = FileUtils.uriToBitmap(context, it)
                selectedImageUri = it
            } catch (e: Exception) {
                scope.launch {
                    snackBarHostState?.showSnackbar(
                        "Failed to load image: ${e.message}",
                        duration = SnackbarDuration.Long
                    )
                }
            }
        }
    }

    LaunchedEffect(successMessage) {
        if (successMessage.isNotEmpty()) {
            scope.launch {
                snackBarHostState?.showSnackbar(
                    message = successMessage,
                    duration = SnackbarDuration.Short
                )
            }
            successMessage = ""
        }
    }
    LaunchedEffect(errorMessage) {
        if (errorMessage.isNotEmpty()) {
            scope.launch {
                snackBarHostState?.showSnackbar(
                    message = errorMessage,
                    duration = SnackbarDuration.Long
                )
            }
            errorMessage = ""
        }
    }

    fun handleChangePassword() {
        val mail = currentUser?.email.orEmpty()
        if (mail.isEmpty()) {
            scope.launch {
                snackBarHostState?.showSnackbar("No email found")
            }
            return
        }
        isChangingPassword = true
        scope.launch {
            try {
                val repo = UserRepository()
                if (repo.checkEmailExists(mail).exists) {
                    sendOTPEmail(
                        context = context,
                        scope = scope,
                        email = mail,
                        onSuccess = {
                            isChangingPassword = false
                            navController?.navigate(Routes.ScreenOTP + "/${getSavedOTP(context)}")
                        },
                        onError = {
                            isChangingPassword = false
                            scope.launch {
                                snackBarHostState?.showSnackbar("OTP error: $it")
                            }
                        }
                    )
                } else {
                    isChangingPassword = false
                    scope.launch {
                        snackBarHostState?.showSnackbar("Email not registered")
                    }
                }
            } catch (e: Exception) {
                isChangingPassword = false
                scope.launch {
                    snackBarHostState?.showSnackbar("Network error")
                }
            }
        }
    }

    val isFullNameValid = fullName.length >= 2
    val isEmailValid = remember(email) { Patterns.EMAIL_ADDRESS.matcher(email).matches() }
    val isContactValid = contact.length >= 8
    val isFormValid = isFullNameValid && isEmailValid && isContactValid
    val isFormChanged = remember(fullName, email, contact, currentUser) {
        fullName != (currentUser?.nom ?: "") ||
                email != (currentUser?.email ?: "") ||
                contact != (currentUser?.contact ?: "")
    }

    if (isLoadingUser) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Primary)
        }
        return
    }

    if (fetchError != null || currentUser == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(fetchError ?: "User not found", color = Color.Red, textAlign = TextAlign.Center)
                Spacer(Modifier.height(16.dp))
                Button(onClick = { forceClearAllData(context); navController?.navigate(Routes.Screen1) }) {
                    Text("Login")
                }
                Spacer(Modifier.height(8.dp))
                Button(onClick = { currentUser = null; fetchError = null; isLoadingUser = true }) {
                    Text("Retry")
                }
            }
        }
        return
    }

    Scaffold(
        topBar = @Composable {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Edit Profile",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp,
                        letterSpacing = 0.15.sp,
                        color = Color(0xFF1A1A1A)
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { navController?.popBackStack() },
                        modifier = Modifier.padding(start = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF424242)
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            forceClearAllData(context)
                            navController?.navigate(Routes.Screen1) {
                                popUpTo(0) { inclusive = true }
                            }
                        },
                        modifier = Modifier.padding(end = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Logout",
                            tint = Color(0xFF424242)
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color(0xFF1A1A1A),
                    navigationIconContentColor = Color(0xFF424242),
                    actionIconContentColor = Color(0xFF424242)
                ),
                modifier = Modifier.shadow(
                    elevation = 2.dp,
                    spotColor = Color.Black.copy(alpha = 0.1f)
                )
            )
        }

    ) { pad ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(pad)
        ) {
            Image(
                painter = painterResource(id = R.drawable.back),
                contentDescription = null,
                contentScale = ContentScale.FillBounds,
                modifier = Modifier.matchParentSize()
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // ------------------- PROFILE IMAGE -------------------
                Box(modifier = Modifier.size(140.dp), contentAlignment = Alignment.BottomEnd) {
                    when {
                        bitmap != null -> Image(
                            bitmap = bitmap!!.asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .border(4.dp, Color.White, CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        currentImageUrl.isNotEmpty() -> {
                            val url = if (currentImageUrl.startsWith("http")) {
                                currentImageUrl.replace("localhost:3005", "10.0.2.2:3005")
                            } else "$BASE_URL/$currentImageUrl"
                            AsyncImage(
                                model = url,
                                contentDescription = null,
                                placeholder = painterResource(R.drawable.logo),
                                error = painterResource(R.drawable.logo),
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                                    .border(4.dp, Color.White, CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        }
                        else -> Image(
                            painter = painterResource(R.drawable.logo),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .border(4.dp, Color.White, CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Surface(
                        modifier = Modifier
                            .size(44.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { galleryLauncher.launch("image/*") },
                        shape = CircleShape,
                        color = Primary,
                        shadowElevation = 6.dp
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = Color.White,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }

                // ------------------- UPDATE IMAGE BUTTON -------------------
                AnimatedVisibility(
                    visible = selectedImageUri != null,
                    enter = fadeIn() + scaleIn(),
                    exit = fadeOut() + scaleOut()
                ) {
                    Spacer(Modifier.height(20.dp))
                    Button(
                        onClick = {
                            if (actualToken.isNotEmpty() && selectedImageUri != null) {
                                isUpdatingImage = true
                                CoroutineScope(Dispatchers.IO).launch {
                                    try {
                                        val repo = UserRepository()
                                        val bytes = FileUtils.uriToByteArray(context, selectedImageUri!!)
                                        val name = FileUtils.getFileName(context, selectedImageUri!!)
                                        if (bytes != null) {
                                            val resp = repo.updateProfileImage(actualToken, bytes, name)
                                            withContext(Dispatchers.Main) {
                                                try {
                                                    val imgResp = repo.getProfileImage(actualToken)
                                                    currentImageUrl = imgResp.imageUrl
                                                } catch (e: Exception) {
                                                    currentImageUrl = "$BASE_URL/${resp.image ?: ""}"
                                                }
                                                currentUser = resp
                                                onProfileUpdated(resp)
                                                successMessage = "Image updated!"
                                                selectedImageUri = null
                                                bitmap = null
                                            }
                                        } else {
                                            withContext(Dispatchers.Main) {
                                                errorMessage = "Image processing error"
                                            }
                                        }
                                    } catch (e: Exception) {
                                        withContext(Dispatchers.Main) {
                                            errorMessage = e.message ?: "Upload failed"
                                        }
                                    } finally {
                                        withContext(Dispatchers.Main) {
                                            isUpdatingImage = false
                                        }
                                    }
                                }
                            }
                        },
                        enabled = !isUpdatingImage,
                        colors = ButtonDefaults.buttonColors(containerColor = Accent),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth(0.75f)
                            .height(48.dp)
                    ) {
                        if (isUpdatingImage) CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(20.dp)
                        )
                        else Text("Update Image", fontWeight = FontWeight.SemiBold)
                    }
                }

                Spacer(Modifier.height(40.dp))

                // ------------------- FORM CARD -------------------
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(12.dp, RoundedCornerShape(20.dp)),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBg)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {

                        ProfileTextField(
                            label = "Name",
                            value = fullName,
                            onValueChange = { fullName = it },
                            placeholder = "Enter your name",
                            leadingIcon = Icons.Default.Person,
                            isError = fullName.isNotEmpty() && !isFullNameValid,
                            supportingText = if (fullName.isNotEmpty() && !isFullNameValid) "At least 2 characters" else null
                        )

                        ProfileTextField(
                            label = "Email",
                            value = email,
                            onValueChange = { email = it },
                            placeholder = "Enter your email",
                            leadingIcon = Icons.Default.Email,
                            keyboardType = KeyboardType.Email,
                            isError = email.isNotEmpty() && !isEmailValid,
                            supportingText = if (email.isNotEmpty() && !isEmailValid) "Invalid email" else null
                        )

                        ProfileTextField(
                            label = "Phone Number",
                            value = contact,
                            onValueChange = { contact = it },
                            placeholder = "Enter your phone",
                            leadingIcon = Icons.Default.Phone,
                            keyboardType = KeyboardType.Phone,
                            isError = contact.isNotEmpty() && !isContactValid,
                            supportingText = if (contact.isNotEmpty() && !isContactValid) "At least 8 digits" else null
                        )

                        currentUser?.createdAt?.let { joined ->
                            Column {
                                Text("Joined at", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF6B7280))
                                Spacer(Modifier.height(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color.White.copy(alpha = 0.7f)
                                ) {
                                    Text(
                                        text = formatDate(joined),
                                        modifier = Modifier.padding(12.dp),
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = Primary
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        // ---- NEW : bouton Analyse CV IA ----
                        Button(
                            onClick = {
                                navController?.navigate("ai_cv")
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFE91E63),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                        ) {
                            Text(
                                "Analyser mon CV avec l'IA",
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(Modifier.height(8.dp))

                        // ---- Change Password ----
                        Button(
                            onClick = { handleChangePassword() },
                            enabled = !isChangingPassword,
                            colors = ButtonDefaults.buttonColors(containerColor = brightRed),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                        ) {
                            if (isChangingPassword) CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            else Text("Change Password", fontWeight = FontWeight.SemiBold)
                        }

                        Spacer(Modifier.height(9.dp))

                        // ---- Save Changes ----
                        Button(
                            onClick = {
                                if (actualToken.isNotEmpty() && isFormValid && isFormChanged) {
                                    isLoading = true
                                    CoroutineScope(Dispatchers.IO).launch {
                                        try {
                                            val repo = UserRepository()
                                            val upd = UpdateUserRequest(fullName, email, contact)
                                            val resp = repo.updateUser(actualToken, upd)
                                            withContext(Dispatchers.Main) {
                                                currentUser = resp
                                                onProfileUpdated(resp)
                                                successMessage = "Profile saved!"
                                            }
                                        } catch (e: Exception) {
                                            withContext(Dispatchers.Main) {
                                                errorMessage = e.message ?: "Save failed"
                                            }
                                        } finally {
                                            withContext(Dispatchers.Main) {
                                                isLoading = false
                                            }
                                        }
                                    }
                                }
                            },
                            enabled = !isLoading && isFormValid && isFormChanged,
                            colors = ButtonDefaults.buttonColors(containerColor = Primary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                        ) {
                            if (isLoading) CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            else Text("Save Changes", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                Spacer(Modifier.height(40.dp))
            }
        }
    }
}

/* -------------------------------------------------------------------------- */
/* TEXT FIELD                                    */
/* -------------------------------------------------------------------------- */
@Composable
fun ProfileTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: ImageVector? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    isError: Boolean = false,
    supportingText: String? = null
) {
    val Primary = Color(0xFF030303)

    Column {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = Color(0xFF6B7280))
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(placeholder, color = Color(0xFF9CA3AF)) },
            leadingIcon = leadingIcon?.let {
                { Icon(it, tint = Primary, contentDescription = null) }
            },
            trailingIcon = {
                if (value.isNotEmpty() && !isError) {
                    Icon(Icons.Default.CheckCircle, tint = Color(0xFF4CAF50), contentDescription = "Valid")
                }
            },
            isError = isError,
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Primary,
                unfocusedBorderColor = Color(0xFFE5E7EB),
                errorBorderColor = Color.Red
            ),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            supportingText = supportingText?.let { { Text(it, color = Color.Red) } }
        )
    }
}

/* -------------------------------------------------------------------------- */
/* DATE FORMAT                                   */
/* -------------------------------------------------------------------------- */
fun formatDate(iso: String?): String {
    if (iso.isNullOrBlank()) return "Unknown"
    return try {
        val input = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.getDefault())
        input.timeZone = java.util.TimeZone.getTimeZone("UTC")
        val date = input.parse(iso) ?: return "Unknown"
        val out = java.text.SimpleDateFormat("dd MMMM yyyy", java.util.Locale.getDefault())
        out.format(date)
    } catch (_: Exception) { "Unknown" }
}

/* -------------------------------------------------------------------------- */
/* PREVIEW                                       */
/* -------------------------------------------------------------------------- */
@Preview(showBackground = true)
@Composable
fun EditProfileScreenPreview() {
    val sample = User(
        _id = "1",
        nom = "John Doe",
        email = "john.doe@example.com",
        contact = "+21612345678",
        role = "user",
        image = "uploads/profile.jpg",
        password = "null",
        createdAt = "2025-11-08T18:47:56.482Z",
        updatedAt = "2025-11-08T18:47:56.482Z"
    )
    EditProfileScreen(user = sample, token = "sample_token")
}
